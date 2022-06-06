FROM python:3.8-buster

# Set env variables
# TODO: remove hardcoding secrets
ENV NEXUS_USER=admin
ENV NEXUS_PASSWORD=q5USphVLSGIzgkiSVGi9HKDiVOb7xbOpf93h7x2VzgBy7wGJp8tvCHhxlcLDsLAZ3qjTap4fK

# Add project
ADD ./ /code
WORKDIR /code

# unzip dependencies
RUN unzip dependencies.zip

RUN rm /code/.git/config
RUN rm -rf .idea && \
    mv /code/dependencies/.ssh /root/.ssh && \
    mv /code/dependencies/.gitconfig /root/.gitconfig && \
    mv /code/dependencies/.git/config /code/.git/config && \
    mv /code/dependencies/* /opt/ && \
    rm dependencies -rf

# Setup Java && Maven
RUN ln -s /opt/jdk-13/bin/* /usr/local/bin/ && \
    ln -s /opt/apache-maven-3.8.3/bin/mvn /usr/local/bin/

# Setup dev environment
RUN python /opt/setup_environment.py && \
    pip install forge-cli==3.0.0b0 && \
    forge install && \
    forge build && \
    forge install-autocomplete

RUN ssh-keyscan git.mindsmiths.com >> ~/.ssh/known_hosts
RUN git pull | true
RUN git branch --all
RUN git checkout demo1/ver3

RUN wget https://raw.githubusercontent.com/git/git/master/contrib/completion/git-completion.bash
RUN mv git-completion.bash /root/.git-completion.bash

CMD ["forge", "run"]
