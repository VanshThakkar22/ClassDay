FROM python:3.12-alpine
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY keep_fresh.py .
ENV TZ=Asia/Kolkata
CMD ["python", "-u", "keep_fresh.py"]
