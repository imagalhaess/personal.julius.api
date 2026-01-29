#!/bin/bash

echo "======================================================"
echo "          JULIUS API - MODO DESENVOLVIMENTO"
echo "======================================================"

# 1. Subir apenas a infraestrutura
echo "[1/3] Subindo infraestrutura (Bancos e Kafka)..."
docker compose up -d user-db transaction-db kafka kafdrop

if [ $? -ne 0 ]; then
    echo "Erro ao subir o Docker. Certifique-se de que o Docker Desktop está rodando."
    exit 1
fi

echo "Aguardando 15 segundos para os serviços estabilizarem..."
sleep 15

# 2. Instalar o ms-common e dependências
echo "[2/3] Instalando dependências e ms-common..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "Erro no build do Maven. Verifique os logs acima."
    exit 1
fi

# 3. Iniciar os serviços
echo "[3/3] Iniciando os microsserviços..."

# Função para abrir nova janela no Windows (Git Bash)
start_service() {
    local name=$1
    local dir=$2
    echo "Iniciando $name..."
    # Usa o comando 'start' do Windows para abrir um novo terminal
    start cmd /k "echo Iniciando $name... && cd $dir && mvn spring-boot:run"
}

start_service "MS-USER" "ms-user"
start_service "MS-TRANSACTION" "ms-transaction"
start_service "MS-PROCESSOR" "ms-processor"

echo "======================================================"
echo "  Infraestrutura OK e Serviços em inicialização!"
echo "  Swagger User: http://localhost:8081/swagger-ui.html"
echo "  Swagger Transaction: http://localhost:8082/swagger-ui.html"
echo "  Kafdrop: http://localhost:9000"
echo "======================================================"
