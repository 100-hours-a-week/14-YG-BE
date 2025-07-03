resource "kafka_topic" "chat_part_message_created" {
  name               = "chat.part.message.created"
  partitions         = 3
  replication_factor = 3

  config = {
    cleanup.policy = "delete"
    retention.ms   = "172800000"
  }
}

resource "kafka_topic" "chat_anon_message_created" {
  name               = "chat.anon.message.created"
  partitions         = 3
  replication_factor = 3

  config = {
    cleanup.policy = "delete"
    retention.ms   = "172800000"
  }
}

resource "kafka_topic" "user_profile_updated" {
  name               = "user.profile.updated"
  partitions         = 2
  replication_factor = 3
}

resource "kafka_topic" "groupbuy_pickup_updated" {
  name               = "groupbuy.pickup.updated"
  partitions         = 2
  replication_factor = 3
}

resource "kafka_topic" "groupbuy_pickup_approaching" {
  name               = "groupbuy.pickup.approaching"
  partitions         = 2
  replication_factor = 3
}

resource "kafka_topic" "groupbuy_due_approaching" {
  name               = "groupbuy.due.approaching"
  partitions         = 2
  replication_factor = 3
}

resource "kafka_topic" "groupbuy_status_closed" {
  name               = "groupbuy.status.closed"
  partitions         = 2
  replication_factor = 3
}

resource "kafka_topic" "groupbuy_status_ended" {
  name               = "groupbuy.status.ended"
  partitions         = 2
  replication_factor = 3
}

resource "kafka_topic" "order_status_pending" {
  name               = "order.status.pending"
  partitions         = 3
  replication_factor = 3
}

resource "kafka_topic" "order_status_confirmed" {
  name               = "order.status.confirmed"
  partitions         = 3
  replication_factor = 3
}

resource "kafka_topic" "order_status_canceled" {
  name               = "order.status.canceled"
  partitions         = 2
  replication_factor = 3
}

resource "kafka_topic" "order_status_refunded" {
  name               = "order.status.refunded"
  partitions         = 2
  replication_factor = 3
}

