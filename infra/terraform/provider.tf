terraform {
  required_providers {
    kafka = {
      source  = "Mongey/kafka"
    }
  }
}

provider "kafka" {
  bootstrap_servers = ["localhost:9092"]
}
