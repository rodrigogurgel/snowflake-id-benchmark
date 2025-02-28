# Comparativo de Geração de IDs: Snowflake vs UUID

Este repositório tem como objetivo comparar o desempenho da geração de IDs utilizando **Snowflake ID** e **UUID**. O benchmark é realizado utilizando a ferramenta [JMH (Java Microbenchmark Harness)](https://www.baeldung.com/java-microbenchmark-harness), permitindo medições precisas de tempo e throughput.

## Tecnologias Utilizadas
- **Kotlin**
- **JMH (Java Microbenchmark Harness)**
- **Kotlin Coroutines**

## Estrutura do Projeto

O projeto contém duas classes principais para os benchmarks:

### `SnowflakeIdBenchmark`
Essa classe mede o tempo de geração de IDs utilizando um **Snowflake ID Generator** personalizado.

> A classe `SnowflakeIdGenerator` foi gerada com o auxílio da AI ChatGPT.

Testes realizados:
1. **`generateSingleSnowflakeId`** - Mede o throughput da geração de um único ID Snowflake por chamada.
2. **`generateBatchSnowflakeIds`** - Mede o tempo médio para geração de 1000 IDs Snowflake sequencialmente.
3. **`generateBatchSnowflakeIdsParallel`** - Mede o tempo médio para geração de 1000 IDs Snowflake em paralelo utilizando coroutines.

### `UuidBenchmark`
Essa classe mede o tempo de geração de IDs utilizando **UUID**.

Testes realizados:
1. **`generateSingleUUID`** - Mede o throughput da geração de um único UUID por chamada.
2. **`generateBatchUUIDs`** - Mede o tempo médio para geração de 1000 UUIDs sequencialmente.
3. **`generateBatchUUIDsParallel`** - Mede o tempo médio para geração de 1000 UUIDs em paralelo utilizando coroutines.

## Como Executar os Benchmarks

Para rodar os benchmarks, utilize o JMH:
```sh
./gradlew benchmark
```
Isso executará todos os testes de performance definidos.

## Resultados Esperados
A expectativa é que o **Snowflake ID** apresente um desempenho superior ao **UUID**, especialmente em cenários de alto throughput, devido à sua implementação baseada em bits e menor complexidade na geração.

## Utilização do Benchmark
A utilização do benchmark se deu pela oportunidade de aplicação prática em uma comparação direta e no entendimento da implementação do mesmo utilizando Kotlin.

## Referências
- [Snowflake ID - Wikipedia](https://en.wikipedia.org/wiki/Snowflake_ID)
- [Kotlinx Benchmark](https://github.com/Kotlin/kotlinx-benchmark)
- [Como fazemos microbenchmarks em Kotlin](https://alice.com.br/tech/como-fazemos-microbenchmarks-em-kotlin/)
- [JMH - Microbenchmark Harness](https://www.baeldung.com/java-microbenchmark-harness)

## Objetivo do Projeto
Este é um projeto de estudos para avaliar a performance do **Snowflake ID Generator** e comparar com a geração de UUIDs utilizando benchmarks.