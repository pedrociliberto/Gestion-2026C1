FROM python:3.15.0a8-trixie

COPY backend /backend
WORKDIR /backend

RUN pip install --upgrade pip
RUN pip install -r requirements.txt

EXPOSE 3000

CMD [ "/bin/bash"]