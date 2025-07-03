terraform {
  required_providers {
    kafka = {
      source  = "Mongey/kafka"
      version = "0.4.16"
    }
  }
}

provider "kafka" {
  bootstrap_servers = ["localhost:9092"]
}
