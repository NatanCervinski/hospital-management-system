# Use a imagem oficial do Node.js
FROM node:20

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia APENAS os arquivos de manifesto de pacotes
COPY package*.json ./

# Instala as dependências da aplicação
# Isso inclui as dependências de produção e desenvolvimento (como o nodemon)
RUN npm install

# Agora copia o resto do código-fonte da aplicação
COPY . .

# Expõe a porta que a aplicação vai rodar
EXPOSE 3000

# O comando padrão para produção (será sobrescrito no modo dev)
CMD [ "node", "src/app.js" ]
