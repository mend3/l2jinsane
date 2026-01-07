# =============================================================================
#                          L2Extalia Server - Makefile
# =============================================================================

DATAPACK_ROOT := .
CONFIG_DIR  := $(DATAPACK_ROOT)/config
BUILD_FILE := $(CONFIG_DIR)/build.properties
OUT_DIR := $(DATAPACK_ROOT)/out
LIBS_DIR := $(DATAPACK_ROOT)/libs

# Server Configuration
APP_NAME := L2Extalia
VERSION  := 1.0.0

# Java Configuration - Override with: make JAVA_HOME=/path/to/jdk run-login
JAVA_HOME ?= /mnt/f/dev/.bin/jdk21
JAVA := $(JAVA_HOME)/bin/java

# JVM Options
JVM_OPTS := -Xms512m -Xmx2g
LOGIN_JVM_OPTS := -Xms256m -Xmx512m

# Classpath - includes out directory and all jars in libs
ifeq ($(OS),Windows_NT)
    CLASSPATH_SEP := ;
    CLASSPATH := $(OUT_DIR);$(LIBS_DIR)/*.jar
else
    CLASSPATH_SEP := :
    CLASSPATH := $(OUT_DIR):$(LIBS_DIR)/*.jar
endif

# Main Classes
LOGIN_SERVER := net.sf.l2j.loginserver.LoginServer
GAME_SERVER := net.sf.l2j.gameserver.GameServer

# Git Information
GIT_COMMIT := $(shell git rev-parse --short HEAD 2>/dev/null || echo "unknown")
BUILD_TIME := $(shell date -u +"%Y-%m-%dT%H:%M:%SZ")

# File patterns for checksum
FILES_REG := \
	-name "*.java" \
	-name "*.jar" \
	-name "*.dtd" \
	-name "*.xsd" \
	-name "*.xml" \
	-name "*.properties" \
	-name "*.sql" \
	-name "*.txt" \
	-name "*.cfg"

SOURCE_DIRS := src

# Compute checksum of source files
SHA256 := $(shell \
    find $(SOURCE_DIRS) -type f \( $(FILES_REG) \) -print0 2>/dev/null | \
    sort -z | \
    xargs -0 sha256sum 2>/dev/null | \
    sha256sum | \
    cut -d' ' -f1 \
)

# =============================================================================
#                              Targets
# =============================================================================

.PHONY: all build clean build-info help debug run-login run-game run-all stop

all: help

# -----------------------------------------------------------------------------
#                          Server Execution
# -----------------------------------------------------------------------------

run-login: ## Start the Login Server
	@echo "Starting Login Server..."
	@echo "Java: $(JAVA)"
	@echo "Classpath: $(CLASSPATH)"
	$(JAVA) $(LOGIN_JVM_OPTS) -cp "$(CLASSPATH)" $(LOGIN_SERVER)

run-game: ## Start the Game Server
	@echo "Starting Game Server..."
	@echo "Java: $(JAVA)"
	@echo "Classpath: $(CLASSPATH)"
	$(JAVA) $(JVM_OPTS) -cp "$(CLASSPATH)" $(GAME_SERVER)

run-login-bg: ## Start Login Server in background (Linux/WSL only)
	@echo "Starting Login Server in background..."
	@nohup $(JAVA) $(LOGIN_JVM_OPTS) -cp "$(CLASSPATH)" $(LOGIN_SERVER) > log/login.log 2>&1 &
	@echo "Login Server started. Check log/login.log for output."

run-game-bg: ## Start Game Server in background (Linux/WSL only)
	@echo "Starting Game Server in background..."
	@nohup $(JAVA) $(JVM_OPTS) -cp "$(CLASSPATH)" $(GAME_SERVER) > log/game.log 2>&1 &
	@echo "Game Server started. Check log/game.log for output."

# -----------------------------------------------------------------------------
#                          Build & Metadata
# -----------------------------------------------------------------------------

build: clean build-info ## Run the build phase
	@echo "Build metadata generated in $(BUILD_FILE)"

build-info: ## Generates the build.properties file
	@mkdir -p $(CONFIG_DIR)
	@echo "build.name=$(APP_NAME)" >  $(BUILD_FILE)
	@echo "build.version=$(VERSION)" >> $(BUILD_FILE)
	@echo "build.time=$(BUILD_TIME)" >> $(BUILD_FILE)
	@echo "build.md5=$(GIT_COMMIT)" >> $(BUILD_FILE)
	@echo "build.sha256=$(SHA256)" >> $(BUILD_FILE)

clean: ## Clean previous build outputs
	@rm -f $(BUILD_FILE)

# -----------------------------------------------------------------------------
#                          Utilities
# -----------------------------------------------------------------------------

debug: ## Show current configuration
	@echo "=== L2Extalia Server Configuration ==="
	@echo "DATAPACK_ROOT: $(DATAPACK_ROOT)"
	@echo "CONFIG_DIR:    $(CONFIG_DIR)"
	@echo "OUT_DIR:       $(OUT_DIR)"
	@echo "LIBS_DIR:      $(LIBS_DIR)"
	@echo "JAVA_HOME:     $(JAVA_HOME)"
	@echo "JAVA:          $(JAVA)"
	@echo "CLASSPATH:     $(CLASSPATH)"
	@echo "GIT_COMMIT:    $(GIT_COMMIT)"
	@echo "BUILD_TIME:    $(BUILD_TIME)"

check-java: ## Verify Java installation
	@echo "Checking Java installation..."
	@$(JAVA) -version

help: ## Display help for each make command
	@echo ""
	@echo "L2Extalia Server - Available Commands"
	@echo "======================================"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
	@echo ""
	@echo "Examples:"
	@echo "  make run-login                    # Start login server"
	@echo "  make run-game                     # Start game server"
	@echo "  make JAVA_HOME=/custom/jdk run-login  # Use custom JDK"
	@echo ""
