FROM docker.io/library/node:22-alpine

RUN npm install -g newman@6

WORKDIR /postman

COPY . .

ENTRYPOINT ["newman", "run", "Network Inventory.postman_collection.json", "-e", "Network Inventory Environment.postman_environment.json"]

CMD ["--folder", "Scenario (E2E)"]