FROM openjdk:17-oracle

RUN apk upgrade --no-cache
RUN apk add --no-cache ca-certificates

COPY bin/callisto-jparest_linux_amd64 /bin/callisto-jparest
