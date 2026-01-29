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

echo "Aguardando 15 segundos para os servicos estabilizarem..."
sleep 15

# 2. Instalar o ms-common e dependencias
echo "[2/3] Instalando dependencias e ms-common..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "Erro no build do Maven. Verifique os logs acima."
    exit 1
fi

# 3. Iniciar os servicos em background
echo "[3/3] Iniciando os microsservicos..."

# Diretorio base
BASE_DIR=$(pwd)

echo "Iniciando MS-USER..."
cd "$BASE_DIR/ms-user" && mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/ms-user.log" 2>&1 &
MS_USER_PID=$!

echo "Iniciando MS-TRANSACTION..."
cd "$BASE_DIR/ms-transaction" && mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/ms-transaction.log" 2>&1 &
MS_TRANSACTION_PID=$!

echo "Iniciando MS-PROCESSOR..."
cd "$BASE_DIR/ms-processor" && mvn spring-boot:run -Dspring-boot.run.profiles=dev > "$BASE_DIR/logs/ms-processor.log" 2>&1 &
MS_PROCESSOR_PID=$!

cd "$BASE_DIR"

echo ""
echo "Aguardando servicos iniciarem (30 segundos)..."
sleep 30

echo ""
echo "======================================================"
echo "  Verificando status dos servicos..."
echo "======================================================"

check_service() {
    local name=$1
    local port=$2
    if curl -s "http://localhost:$port/actuator/health" | grep -q "UP"; then
        echo "  [OK] $name (porta $port)"
    else
        echo "  [ERRO] $name (porta $port) - verifique logs/$name.log"
    fi
}

check_service "ms-user" 8081
check_service "ms-transaction" 8082
check_service "ms-processor" 8080

echo ""
echo "======================================================"
echo "  URLs dos Servicos:"
echo "  - MS-USER:        http://localhost:8081"
echo "  - MS-TRANSACTION: http://localhost:8082"
echo "  - MS-PROCESSOR:   http://localhost:8080"
echo ""
echo "  Documentacao (Swagger):"
echo "  - User:        http://localhost:8081/swagger-ui.html"
echo "  - Transaction: http://localhost:8082/swagger-ui.html"
echo ""
echo "  Monitoramento:"
echo "  - Kafdrop:     http://localhost:9000"
echo ""
echo "  Logs em: $BASE_DIR/logs/"
echo "======================================================"
echo ""
echo "Para parar os servicos: pkill -f 'spring-boot:run'"
