#!/bin/bash
# create-topics.sh

BROKER="kafka-1:9092"

# 채팅
docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 3 --replication-factor 3 --topic chat.part.message.created

docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 3 --replication-factor 3 --topic chat.anon.message.created

# 유저 프로필
docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 2 --replication-factor 3 --topic user.profile.updated

# 공구 픽업일 변경 & 알림
docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 2 --replication-factor 3 --topic groupbuy.pickup.updated

docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 2 --replication-factor 3 --topic groupbuy.pickup.approaching

docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 2 --replication-factor 3 --topic groupbuy.due.approaching

# 공구 상태
docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 2 --replication-factor 3 --topic groupbuy.status.closed

docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 2 --replication-factor 3 --topic groupbuy.status.ended

# 주문 상태
docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 3 --replication-factor 3 --topic order.status.pending

docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 3 --replication-factor 3 --topic order.status.confirmed

docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 2 --replication-factor 3 --topic order.status.canceled

docker exec -it kafka-1 kafka-topics.sh --create --bootstrap-server ${BROKER} \
  --partitions 2 --replication-factor 3 --topic order.status.refunded
