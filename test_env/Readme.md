This is a folder simply to facilitate the setup of a local docker based
Kafka cluster, which will have appd Agent hooks for as well.

It can be used to vet the extension but will also have java agent
instrumentation in them so we can test against a controller numerous
aspects of kafka monitoring with AppD.

1. Make sure you did a build so there is a Kafka zip in project_root/target
2. CD into this directory.
2. Make sure you create this docker network with: docker network create kafkakraft-network
3. Run the command: docker compose -f kafka-cluster-kraft.yml up -d --build
3. You can then docker exec into any of the brokers
4. In the users home is a data_gen_and_load.py that you can run with python
5. It will create a test topic on that broker and send/receive messages


Rebuild/Deploy
1. Run docker compose -f kafka-cluster-kraft.yml down
2. Run your maven build to regenerate zip
3. Run docker compose -f kafka-cluster-kraft.yml up -d --build