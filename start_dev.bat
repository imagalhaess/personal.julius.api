@echo off
setlocal enabledelayedexpansion

echo ======================================================
echo           JULIUS API - MODO DESENVOLVIMENTO
echo ======================================================
echo.

REM Diretorio base
set BASE_DIR=%~dp0
cd /d %BASE_DIR%

REM Criar pasta de logs se nao existir
if not exist "logs" mkdir logs

echo [1/4] Subindo infraestrutura (Bancos e Kafka)...
docker compose up -d user-db transaction-db kafka kafdrop

if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha ao subir Docker. Certifique-se de que o Docker Desktop esta rodando.
    pause
    exit /b 1
)

echo Aguardando 15 segundos para os servicos estabilizarem...
timeout /t 15 /nobreak > nul

echo.
echo [2/4] Instalando dependencias e ms-common...
call mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo ERRO: Falha no build do Maven. Verifique os logs acima.
    pause
    exit /b 1
)

echo.
echo [3/4] Iniciando os microsservicos...
echo.

REM Iniciar MS-USER em nova janela
echo Iniciando MS-USER na porta 8081...
start "MS-USER" cmd /c "cd /d %BASE_DIR%ms-user && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

REM Aguardar um pouco entre cada servico
timeout /t 5 /nobreak > nul

REM Iniciar MS-TRANSACTION em nova janela
echo Iniciando MS-TRANSACTION na porta 8082...
start "MS-TRANSACTION" cmd /c "cd /d %BASE_DIR%ms-transaction && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

timeout /t 5 /nobreak > nul

REM Iniciar MS-PROCESSOR em nova janela
echo Iniciando MS-PROCESSOR na porta 8080...
start "MS-PROCESSOR" cmd /c "cd /d %BASE_DIR%ms-processor && mvn spring-boot:run -Dspring-boot.run.profiles=dev"

echo.
echo [4/4] Aguardando servicos iniciarem (45 segundos)...
timeout /t 45 /nobreak > nul

echo.
echo ======================================================
echo   Verificando status dos servicos...
echo ======================================================
echo.

REM Verificar MS-USER
curl -s http://localhost:8081/actuator/health | findstr /C:"UP" > nul
if %ERRORLEVEL% EQU 0 (
    echo   [OK] MS-USER        - http://localhost:8081
) else (
    echo   [ERRO] MS-USER      - Verifique a janela do servico
)

REM Verificar MS-TRANSACTION
curl -s http://localhost:8082/actuator/health | findstr /C:"UP" > nul
if %ERRORLEVEL% EQU 0 (
    echo   [OK] MS-TRANSACTION - http://localhost:8082
) else (
    echo   [ERRO] MS-TRANSACTION - Verifique a janela do servico
)

REM Verificar MS-PROCESSOR
curl -s http://localhost:8080/actuator/health | findstr /C:"UP" > nul
if %ERRORLEVEL% EQU 0 (
    echo   [OK] MS-PROCESSOR   - http://localhost:8080
) else (
    echo   [ERRO] MS-PROCESSOR - Verifique a janela do servico
)

echo.
echo ======================================================
echo   URLs dos Servicos:
echo   - MS-USER:        http://localhost:8081
echo   - MS-TRANSACTION: http://localhost:8082
echo   - MS-PROCESSOR:   http://localhost:8080
echo.
echo   Documentacao (Swagger):
echo   - User:        http://localhost:8081/swagger-ui.html
echo   - Transaction: http://localhost:8082/swagger-ui.html
echo.
echo   Monitoramento:
echo   - Kafdrop:     http://localhost:9000
echo ======================================================
echo.
echo Os servicos estao rodando em janelas separadas.
echo Para parar, feche as janelas ou use: taskkill /F /IM java.exe
echo.
pause
