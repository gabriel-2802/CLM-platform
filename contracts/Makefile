build:
    docker build -t clm-contracts .
run:
    docker run -p 8080:8080 \
        -e SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/clm_platform \
        -e SPRING_DATASOURCE_USERNAME=clm_user \
        -e SPRING_DATASOURCE_PASSWORD=clm_password \
        clm-contracts
