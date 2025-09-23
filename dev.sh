#!/bin/bash
# =============================================================================
# dev.sh - Script único para automação de ambiente local (PostgreSQL + ferramentas)
# =============================================================================

set -e

SERVICE_NAME="crm-postgres"
BACKUP_DIR="backups"
VOLUME_NAME="$(basename $PWD)_postgres_data"

function check_docker() {
  if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Inicie o Docker."
    exit 1
  fi
}

function start_db() {
  echo "🐘 Iniciando PostgreSQL..."
  check_docker
  docker-compose up -d $SERVICE_NAME
  sleep 15
  if docker-compose ps $SERVICE_NAME | grep -q "healthy"; then
    echo "✅ PostgreSQL rodando em jdbc:postgresql://localhost:5432/crm_db"
  else
    echo "❌ Falha ao iniciar PostgreSQL. Logs:"
    docker-compose logs $SERVICE_NAME
    exit 1
  fi
}

function stop_db() {
  echo "🛑 Parando PostgreSQL..."
  docker-compose down
  echo "✅ PostgreSQL parado!"
}

function start_with_tools() {
  echo "🛠️ Iniciando PostgreSQL + pgAdmin + MailHog..."
  check_docker
  docker-compose --profile tools up -d
  sleep 20
  echo "✅ Serviços iniciados!"
  echo "📊 PostgreSQL: localhost:5432"
  echo "🔧 pgAdmin: http://localhost:5050 (admin@crm.local / admin123)"
  echo "📧 MailHog: http://localhost:8025 (Web UI)"
  echo "📨 SMTP Server: localhost:1025"
}

function start_mail() {
  echo "📧 Iniciando apenas MailHog..."
  check_docker
  docker-compose up -d crm-mailhog
  sleep 5
  echo "✅ MailHog iniciado!"
  echo "📧 MailHog Web UI: http://localhost:8025"
  echo "📨 SMTP Server: localhost:1025"
}

function stop_mail() {
  echo "🛑 Parando MailHog..."
  docker-compose stop crm-mailhog
  docker-compose rm -f crm-mailhog
  echo "✅ MailHog parado!"
}

function restart_db() {
  echo "🔄 Reiniciando PostgreSQL..."
  docker-compose down
  sleep 3
  docker-compose up -d $SERVICE_NAME
  echo "✅ Reiniciado!"
}

function logs_db() {
  echo "📋 Logs do PostgreSQL:"
  docker-compose logs -f $SERVICE_NAME
}

function logs_mail() {
  echo "📋 Logs do MailHog:"
  docker-compose logs -f crm-mailhog
}

function logs_all() {
  echo "📋 Logs de todos os serviços:"
  docker-compose logs -f
}

function connect_db() {
  echo "🔌 Conectando ao PostgreSQL..."
  docker-compose exec $SERVICE_NAME psql -U crm_user -d crm_db
}

function backup_db() {
  mkdir -p $BACKUP_DIR
  FILE="$BACKUP_DIR/backup-$(date +%Y%m%d-%H%M%S).sql"
  echo "💾 Criando backup em $FILE..."
  docker-compose exec $SERVICE_NAME pg_dump -U crm_user -d crm_db > $FILE
  echo "✅ Backup concluído."
}

function restore_db() {
  if [ -z "$1" ]; then
    echo "❌ Uso: $0 restore <arquivo.sql>"
    ls -la $BACKUP_DIR/*.sql 2>/dev/null || echo "Nenhum backup encontrado"
    exit 1
  fi
  echo "🔄 Restaurando banco de $1..."
  docker-compose exec -T $SERVICE_NAME psql -U crm_user -d crm_db < "$1"
  echo "✅ Restaurado com sucesso!"
}

function reset_db() {
  echo "⚠️ Isto apagará TODOS os dados!"
  read -p "Digite 'yes' para confirmar: " confirm
  if [ "$confirm" = "yes" ]; then
    echo "🗑️ Removendo volume de dados..."
    docker-compose down
    docker volume rm $VOLUME_NAME || true
    echo "🐘 Subindo PostgreSQL limpo..."
    docker-compose up -d $SERVICE_NAME
    echo "✅ Banco resetado!"
  else
    echo "❌ Cancelado."
  fi
}

function check_services() {
  echo "🔍 Verificando serviços..."

  # Check PostgreSQL
  if docker-compose ps $SERVICE_NAME | grep -q "Up"; then
    echo "✅ PostgreSQL rodando"
    if docker-compose exec $SERVICE_NAME pg_isready -U crm_user -d crm_db >/dev/null 2>&1; then
      echo "✅ PostgreSQL - Conexão OK"
      TABLES=$(docker-compose exec $SERVICE_NAME psql -U crm_user -d crm_db -t -c \
        "SELECT count(*) FROM information_schema.tables WHERE table_schema='public';" | tr -d ' ')
      echo "📊 PostgreSQL - Tabelas: $TABLES"
    else
      echo "❌ PostgreSQL não responde a conexões"
    fi
  else
    echo "❌ PostgreSQL não está rodando"
  fi

  # Check pgAdmin
  if docker-compose ps crm-pgadmin | grep -q "Up"; then
    echo "✅ pgAdmin rodando em http://localhost:5050"
  else
    echo "⚪ pgAdmin não está rodando"
  fi

  # Check MailHog
  if docker-compose ps crm-mailhog | grep -q "Up"; then
    echo "✅ MailHog rodando em http://localhost:8025"
  else
    echo "⚪ MailHog não está rodando"
  fi
}

function status() {
  echo "📊 Status dos serviços:"
  docker-compose ps
}

function usage() {
  echo "Uso: $0 <comando>"
  echo ""
  echo "📊 Comandos principais:"
  echo "  start        - Inicia apenas PostgreSQL"
  echo "  stop         - Para PostgreSQL"
  echo "  tools        - Inicia PostgreSQL + pgAdmin + MailHog"
  echo "  restart      - Reinicia PostgreSQL"
  echo ""
  echo "📧 Comandos de email:"
  echo "  mail-start   - Inicia apenas MailHog"
  echo "  mail-stop    - Para MailHog"
  echo ""
  echo "📋 Logs e monitoramento:"
  echo "  logs         - Logs do PostgreSQL"
  echo "  logs-mail    - Logs do MailHog"
  echo "  logs-all     - Logs de todos os serviços"
  echo "  check        - Verifica status detalhado"
  echo "  status       - Status resumido dos serviços"
  echo ""
  echo "🗄️ Gerenciamento de dados:"
  echo "  connect      - Conecta via psql"
  echo "  backup       - Faz backup do banco"
  echo "  restore <f>  - Restaura backup"
  echo "  reset        - Reseta banco (DANGER!)"
  echo ""
  echo "📧 URLs úteis:"
  echo "  PostgreSQL: jdbc:postgresql://localhost:5432/crm_db"
  echo "  pgAdmin: http://localhost:5050 (admin@crm.local / admin123)"
  echo "  MailHog: http://localhost:8025 (Web UI)"
  echo "  SMTP: localhost:1025"
  echo ""
}

case "$1" in
  start) start_db ;;
  stop) stop_db ;;
  tools) start_with_tools ;;
  restart) restart_db ;;
  mail-start) start_mail ;;
  mail-stop) stop_mail ;;
  logs) logs_db ;;
  logs-mail) logs_mail ;;
  logs-all) logs_all ;;
  connect) connect_db ;;
  backup) backup_db ;;
  restore) restore_db "$2" ;;
  reset) reset_db ;;
  check) check_services ;;
  status) status ;;
  *) usage ;;
esac