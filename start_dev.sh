#!/bin/bash

set -e

echo "======================================================"
echo "          JULIUS API - MODO DESENVOLVIMENTO"
echo "======================================================"
echo

# Diretorio base (onde o script esta localizado)
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$BASE_DIR"

# Criar pasta de logs se nao existir
mkdir -p logs

echo "[1/4] Subindo infraestrutura (Bancos e Kafka)..."
docker compose -f docker-compose.dev.yml up -d

if [ $? -ne 0 ]; then
    echo "ERRO: Falha ao subir Docker. Certifique-se de que o Docker esta rodando."
    exit 1
fi

echo "Aguardando 15 segundos para os servicos estabilizarem..."
sleep 15

echo
echo "[2/4] Instalando dependencias e ms-common..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "ERRO: Falha no build do Maven. Verifique os logs acima."
    exit 1
fi

echo
echo "[3/4] Iniciando os microsservicos em terminais separados..."
echo

# Funcao para detectar e usar o terminal disponivel
open_terminal() {
    local title="$1"
    local dir="$2"
    local cmd="$3"

    if command -v gnome-terminal &> /dev/null; then
        gnome-terminal --title="$title" -- bash -c "cd $dir && $cmd; exec bash"
    elif command -v konsole &> /dev/null; then
        konsole --new-tab -p tabtitle="$title" -e bash -c "cd $dir && $cmd; exec bash"
    elif command -v xterm &> /dev/null; then
        xterm -T "$title" -e "cd $dir && $cmd; exec bash" &
    elif command -v xfce4-terminal &> /dev/null; then
        xfce4-terminal --title="$title" -e "bash -c 'cd $dir && $cmd; exec bash'" &
    else
        # Fallback: rodar em background com log
        echo "Nenhum terminal grafico encontrado. Executando em background..."
        echo "Logs serao salvos em logs/${title}.log"
        cd "$dir" && nohup bash -c "$cmd" > "../logs/${title}.log" 2>&1 &
        cd "$BASE_DIR"
    fi
}

# Iniciar MS-USER em nova janela
echo "Iniciando MS-USER na porta 8081..."
open_terminal "MS-USER [8081]" "${BASE_DIR}/ms-user" "mvn spring-boot:run -Dspring-boot.run.profiles=dev"

# Aguardar um pouco entre cada servico
sleep 5

# Iniciar MS-TRANSACTION em nova janela
echo "Iniciando MS-TRANSACTION na porta 8082..."
open_terminal "MS-TRANSACTION [8082]" "${BASE_DIR}/ms-transaction" "mvn spring-boot:run -Dspring-boot.run.profiles=dev"

sleep 5

# Iniciar MS-PROCESSOR em nova janela
echo "Iniciando MS-PROCESSOR na porta 8080..."
open_terminal "MS-PROCESSOR [8080]" "${BASE_DIR}/ms-processor" "mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo
echo "[4/4] Aguardando servicos iniciarem (60 segundos)..."
sleep 60

echo
echo "======================================================"
echo "  Verificando status dos servicos..."
echo "======================================================"
echo

# Verificar MS-USER
if curl -s http://localhost:8081/actuator/health | grep -q "UP"; then
    echo "  [OK] MS-USER        - http://localhost:8081"
else
    echo "  [!!] MS-USER        - Aguardando... verifique o terminal"
fi

# Verificar MS-TRANSACTION
if curl -s http://localhost:8082/actuator/health | grep -q "UP"; then
    echo "  [OK] MS-TRANSACTION - http://localhost:8082"
else
    echo "  [!!] MS-TRANSACTION - Aguardando... verifique o terminal"
fi

# Verificar MS-PROCESSOR
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "  [OK] MS-PROCESSOR   - http://localhost:8080"
else
    echo "  [!!] MS-PROCESSOR   - Aguardando... verifique o terminal"
fi

echo
echo "======================================================"
echo "  URLs dos Servicos:"
echo "  - MS-USER:        http://localhost:8081"
echo "  - MS-TRANSACTION: http://localhost:8082"
echo "  - MS-PROCESSOR:   http://localhost:8080"
echo
echo "  Swagger UI:"
echo "  - User:        http://localhost:8081/swagger-ui/index.html"
echo "  - Transaction: http://localhost:8082/swagger-ui/index.html"
echo
echo "  Monitoramento:"
echo "  - Kafdrop:     http://localhost:9000"
echo "======================================================"
echo
echo "DICA: Cada servico roda em um terminal separado."
echo "      Os terminais permanecem abertos para ver os logs."
echo "      Para parar tudo: feche os terminais ou use: pkill -f 'spring-boot:run'"
echo
read -p "Pressione Enter para sair..."
