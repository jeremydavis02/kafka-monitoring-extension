import os
import socket
import subprocess
import threading
import time

hostname = socket.gethostname()
bootstrap_port = os.environ['BOOTSTRAP_PORT']
os.system(f'kafka-topics --bootstrap-server localhost:{bootstrap_port} --create --topic {hostname}.test.data')

def start_producing():
    # now we spin up process calling producer script and pushing messages

    with subprocess.Popen(
            f'/usr/bin/kafka-console-producer --bootstrap-server localhost:{bootstrap_port} --topic {hostname}.test.data',
            shell=True, stdin=subprocess.PIPE) as producer_process:
        with open('.bashrc') as msg_file:
            line = msg_file.readline()
            producer_process.stdin.write(line)
            print(f'Producer sent message:{line}')
            time.sleep(60)  #sleep a minute before next read

# we spin up consumer for topic to consume
def start_consuming(consumer_name):
    with subprocess.Popen(
            f'/usr/bin/kafka-console-consumer --bootstrap-server localhost:{bootstrap_port} --topic {hostname}.test.data',
            shell=True, stdout=subprocess.PIPE) as consumer_process:
        for line in consumer_process.stdout:
            print(f'Consumer Named:{consumer_name} received:{line}')

if __name__ == "__main__":
    producer_thread = threading.Thread(target=start_producing)
    c1_thread = threading.Thread(target=start_consuming(f'{hostname}_consumer1'))
    c2_thread = threading.Thread(target=start_consuming(f'{hostname}_consumer2'))
    producer_thread.start()
    c1_thread.start()
    c2_thread.start()

