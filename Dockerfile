# 1단계: Builder
FROM python:3.11-slim AS builder

WORKDIR /app

COPY requirements.txt .
RUN pip install --upgrade pip && pip install --no-cache-dir -r requirements.txt

# 2단계: Runtime
FROM python:3.11-slim

WORKDIR /app

# 런타임에는 site-packages만 복사 (실행에 필요한 패키지만)
COPY --from=builder /usr/local/lib/python3.11 /usr/local/lib/python3.11
COPY --from=builder /usr/local/bin /usr/local/bin

# 소스코드만 선택적으로 복사 (환경 변수 제외)
COPY app/ ./
# 포트 명시 (선택)
EXPOSE 8100

ENTRYPOINT ["bash", "-c", "uvicorn main:app --host 0.0.0.0 --port 8100 --log-level debug --access-log | tee -a /var/log/moongsan/ai_moongsan.log"]