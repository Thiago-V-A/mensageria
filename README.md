# Kafka Messaging System

Este projeto simula uma plataforma de comércio eletrônico utilizando Apache Kafka como backbone de mensageria, com três microsserviços desenvolvidos em Java com Spring Boot. O objetivo é projetar, implementar e documentar um sistema distribuído que usa Kafka para processar eventos em tempo real, explorando tópicos, partições, entre outros conceitos.

## Cenário Proposto

* Order-Service (Produtor): expõe uma API REST para criação de pedidos (POST /orders) que gera um UUID, timestamp e lista de itens. Ele publica esses pedidos confirmados no tópico Kafka `orders`.
* Inventory-Service (Consumidor + Produtor): consome mensagens do tópico `orders`, simula a reserva de estoque (se a quantidade de itens for menor ou igual a 5, considera sucesso; caso contrário, falha) e publica o resultado no tópico `inventory-events`.
* Notification-Service (Consumidor): consome mensagens do tópico `inventory-events` e simula o envio de e-mails/SMS registrando a notificação no console.

## Requisitos

Para executar este projeto, você precisará ter instalado:

* Docker e Docker Compose
* Java 17+ e Maven (para rodar os serviços Spring Boot manualmente)

## Subindo o Kafka

Para iniciar o broker Kafka e o Zookeeper, utilize o Docker Compose:

docker-compose up -d

**Criação de Tópicos:**
Os tópicos `orders` e `inventory-events` são necessários para o funcionamento do sistema. Embora o Spring Kafka possa criá-los automaticamente na primeira mensagem, você pode criá-los manualmente via `kafka-topics.sh` se desejar garantir sua existência:

```bash
# Exemplo para criar o tópico 'orders'
docker exec kafka kafka-topics --create --topic orders --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# Exemplo para criar o tópico 'inventory-events'
docker exec kafka kafka-topics --create --topic inventory-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

*Note: O número de partições e o fator de replicação podem ser ajustados conforme a necessidade do ambiente. No `docker-compose.yml`, o `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR` é 1.*

## Rodando os Serviços Spring Boot

Com o Kafka rodando, compile e execute cada serviço em terminais separados:

1.  Entre na pasta `order-service`:
    ```bash
    cd order-service
    mvn spring-boot:run
    ```
2.  Entre na pasta `inventory-service`:
    ```bash
    cd inventory-service
    mvn spring-boot:run
    ```
3.  Entre na pasta `notification-service`:
    ```bash
    cd notification-service
    mvn spring-boot:run
    ```

## Testando a Solução

Envie um pedido para o Order-Service usando `curl`. Observe a saída nos consoles do `Inventory-Service` e `Notification-Service`.

**Exemplo de Pedido (sucesso esperado para 2 itens):**

```bash
curl -X POST http://localhost:8082/orders -H "Content-Type: application/json" -d '{"items": ["item1", "item2"]}'
```

*Note: A porta do Order-Service é 8082, conforme `application.yml`.*

**Exemplo de Pedido (falha de estoque esperada para 6 itens):**

```bash
curl -X POST http://localhost:8082/orders -H "Content-Type: application/json" -d '{"items": ["item1", "item2", "item3", "item4", "item5", "item6"]}'
```

## Requisitos Não-Funcionais

### 1\. Escalabilidade

A escalabilidade pode ser alcançada com o Apache Kafka de várias maneiras:

  * **Partições:** Os tópicos Kafka são divididos em partições. Cada partição é uma sequência de mensagens ordenada e imutável. Isso permite que os dados sejam distribuídos entre vários brokers no cluster Kafka e que múltiplos consumidores leiam em paralelo.
  * **Grupos de Consumidores:** Múltiplas instâncias de um mesmo serviço consumidor podem formar um grupo de consumidores. Dentro de um grupo, cada partição é atribuída a uma única instância de consumidor, permitindo que o processamento seja escalado horizontalmente. Se um novo consumidor é adicionado ao grupo, as partições são redistribuídas automaticamente entre as instâncias.
  * **Adição de Brokers:** É possível adicionar mais brokers ao cluster Kafka para aumentar a capacidade de armazenamento e processamento.

### 2\. Tolerância a Falhas

Tolerância a falhas significa que o sistema pode continuar operando mesmo na ocorrência de falhas em seus componentes. O Kafka é projetado para alta disponibilidade:

  * **Replicação de Dados:** As partições Kafka podem ser replicadas em vários brokers. Se um broker falhar, as réplicas em outros brokers podem assumir o papel de líder, garantindo que os dados permaneçam acessíveis e que a produção/consumo possa continuar. No `docker-compose.yml` atual, o fator de replicação para `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR` é 1, mas em um ambiente de produção, um fator de replicação maior que 1 (e.g., 3) seria usado para maior resiliência.
  * **Resiliência de Produtores/Consumidores:** Se uma instância do `Order-Service` ou `Inventory-Service` falhar, outras instâncias em execução (se houver) podem continuar a processar. No caso dos consumidores, o Kafka rastreia os offsets, permitindo que um consumidor reiniciado ou um novo consumidor no grupo retome o processamento de onde parou, sem perda de dados.

### 3\. Idempotência

Idempotência significa que uma operação pode ser executada várias vezes sem causar efeitos colaterais adicionais além do primeiro sucesso. Para produtores Kafka, isso significa que enviar a mesma mensagem múltiplas vezes devido a retentativas de rede, por exemplo, não resultará em mensagens duplicadas no tópico.

  * **Garantia no Kafka:** Desde o Kafka 0.11.0.0, os produtores podem ser configurados para serem idempotentes (o `enable.idempotence` é `true` por padrão para o `DefaultKafkaProducerFactory` do Spring Kafka quando se usa a versão 3.1.0+ do Spring Boot como no `pom.xml`). Isso é alcançado através de um Producer ID (PID) e números de sequência para cada mensagem. O broker usa essa informação para garantir que mensagens duplicadas de retentativas não sejam persistidas.
  * **No Cenário:** O `Order-Service` gera um `orderId` único (UUID) para cada pedido. Embora a idempotência do produtor Kafka garanta que a mensagem não seja duplicada no tópico, ter um ID único para o pedido é crucial para a idempotência no lado do consumidor. Se o `Inventory-Service` ou `Notification-Service` recebessem a mesma mensagem duas vezes por algum motivo (ex: falha após processar, mas antes de commitar o offset), eles poderiam usar o `orderId` para identificar e evitar o processamento duplicado da lógica de negócio.

## Membros do Grupo

  * Carlos Antonio
  * Thiago Vicente 

## Links Úteis

  * [Documentação do Spring Kafka](https://docs.spring.io/spring-kafka/)
  * [Apache Kafka Quickstart](https://kafka.apache.org/quickstart)
  * [Documentação oficial Kafka 4.0.0](https://www.google.com/search?q=https://kafka.apache.org/documentation/%23quickstart) 

