# Etapa 1: Define a imagem base do Node.js, que contém o npm e o Node.
# A versão 20 é uma boa escolha LTS (Long-Term Support).
FROM node:20

# Etapa 2: Define o diretório de trabalho dentro do contêiner.
# Todos os comandos a seguir serão executados a partir desta pasta.
WORKDIR /app

# Etapa 3: Copia os arquivos de manifesto de pacotes primeiro.
# O Docker só irá reexecutar o passo seguinte (npm install) se estes arquivos mudarem.
# Isso cria uma camada de cache para as dependências, agilizando muito os builds.
COPY package*.json ./

# Etapa 4: Instala todas as dependências listadas no package.json.
# Isso inclui o Angular CLI e outras dependências de desenvolvimento.
RUN npm install

# Etapa 5: Copia o restante do código-fonte da aplicação para o diretório de trabalho.
# Como as dependências já foram instaladas, mudanças no código não invalidarão o cache do npm install.
COPY . .

# Etapa 6: Expõe a porta padrão que o servidor de desenvolvimento do Angular usa.
EXPOSE 4200

# Etapa 7: Define o comando padrão para iniciar a aplicação.
# O `npm start` em um projeto Angular geralmente executa "ng serve".
# Este comando será usado pelo seu `docker-compose.dev.yml`.
CMD ["npm", "start"]
