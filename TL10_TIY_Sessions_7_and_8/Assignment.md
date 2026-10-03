# TL10 Assignment: Asynchronous Order Processing System

## 📝 Assignment Overview

Design and implement an **asynchronous order processing system** using JMS queues and an MDB.

## 🏢 Business Scenario

When a customer places an order, it must not slow down the web request. Instead, the web tier places the order onto a JMS Queue and immediately returns a confirmation. An MDB picks it up in the background and processes it (inventory check + simulated shipping notice).

## 📋 Requirements

1. **The Order POJO:** Create a `Serializable` class `Order` with fields: `orderId`, `productName`, `quantity`, `customerEmail`.
2. **The Producer Bean:** A `@Stateless` EJB `OrderDispatchService` that injects a `Queue` and `ConnectionFactory` and sends an `ObjectMessage` containing the `Order`.
3. **The MDB Consumer:** An `OrderProcessorMDB` that implements `MessageListener`, extracts the `Order`, and prints a processing confirmation.
4. **The Web Trigger:** A JSF backing bean with a form for order details and a button that calls `OrderDispatchService.dispatchOrder(order)`.

## 💯 Grading

| Area | Marks |
|---|---|
| Order POJO is Serializable and properly structured | 20% |
| Producer correctly sends ObjectMessage to a Queue | 35% |
| MDB correctly receives, casts, and processes the Order | 35% |
| Web form triggers the async flow without blocking | 10% |
