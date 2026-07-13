# 🌊 Breeze-Batch

<p align="center">
  <img src="[https://capsule-render.vercel.app/api?type=waving&color=auto&height=220&section=header&text=Breeze-Batch&fontSize=80](https://capsule-render.vercel.app/api?type=waving&color=auto&height=220&section=header&text=Breeze-Batch&fontSize=80)" alt="Header" />
</p>

<p align="center">
  <img src="[https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)" alt="Java"/>
  <img src="[https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen?style=flat-square&logo=springboot](https://img.shields.io/badge/Spring%20Boot-3.2.x-brightgreen?style=flat-square&logo=springboot)" alt="SpringBoot"/>
  <img src="[https://img.shields.io/badge/Spring%20Batch-5.1.x-blue?style=flat-square&logo=spring](https://img.shields.io/badge/Spring%20Batch-5.1.x-blue?style=flat-square&logo=spring)" alt="SpringBatch"/>
  <img src="[https://img.shields.io/badge/PostgreSQL-15.x-blueviolet?style=flat-square&logo=postgresql](https://img.shields.io/badge/PostgreSQL-15.x-blueviolet?style=flat-square&logo=postgresql)" alt="PostgreSQL"/>
  <img src="[https://img.shields.io/badge/Docker-Latest-blue?style=flat-square&logo=docker](https://img.shields.io/badge/Docker-Latest-blue?style=flat-square&logo=docker)" alt="Docker"/>
</p>

> **Breeze-Batch is a production-oriented Spring Batch framework designed to explore real-world batch operations beyond simple Reader-Processor-Writer implementations.**
> 
> *본 프로젝트는 단순한 배치 기능 구현을 넘어, 분산 환경에서의 안정성, 실패 복구(Restartability), 즉각적인 장애 가시성(Observability) 확보 등 엔터프라이즈 레벨의 배치 운영 플랫폼 표준을 지향합니다.*

---

## ✨ Project Overview

많은 Spring Batch 레퍼런스는 단순히 `Reader` -> `Processor` -> `Writer`를 어떻게 조합하는지에 초점을 맞춥니다. 하지만 **실제 운영(Production) 환경**에서는 비즈니스 로직만큼이나 아래의 요소들이 중요합니다.

* **복구력 (Resilience):** 실패한 배치의 정확한 이력 추적 및 유연한 재시작
* **가시성 (Observability):** 장애 실시간 전파 및 실행 로그 영속화
* **데이터 정합성 (Integrity):** 분산 인스턴스 환경에서의 동시 실행 및 중복 처리 방지

**Breeze-Batch**는 이러한 운영 관점(Operation)의 핵심 요구사항들을 집약하여, 안정적이고 재사용 가능한 배치 아키텍처 인프라를 제공합니다.

---

## 🎯 Key Design Points

1. **Spring Batch 5 아키텍처 최적화:** 최신 버전의 변경점을 반영하여 명시적이고 유연한 빌더 구조 채택
2. **Strategy Pattern 기반 알림 인프라:** 인프라 변경(Slack, Mail, 카카오 등)에 유연하게 대응하는 비동기 알림 디스패처 구축
3. **무중단 복구 프로세스:** `JobExplorer`와 `JobOperator`를 REST API와 결합하여 외부 인터페이스를 통한 실시간 관리 체계 마련
4. **분산 락 기반 동시성 제어:** 클러스터링 환경에서 발생할 수 있는 스케줄러 중복 실행 리스크 원천 차단

---

## 🏛 Architecture

```text
                      ┌──────────────────────────────┐
                      │    External REST API Client  │
                      └──────────────┬───────────────┘
                                     │ (HTTP Request)
                                     ▼
                        [ BatchAdminController ]
                                     │
           ┌─────────────────────────┴─────────────────────────┐
           ▼                                                   ▼
    [ JobLauncher ]                                 [ BatchRestartService ]
           │                                                   │
           ▼ (Invoke Job)                                      ▼ (Fetch / Re-run)
 ┌───────────────────┐                               ┌───────────────────┐
 │  Spring Batch Job │                               │    JobExplorer    │
 └─────────┬─────────┘                               │    JobOperator    │
           │                                         └───────────────────┘
           ▼ (Step Execution)
   ┌───────────────┐
   │  Batch Step   │
   └───────┬───────┘
           │
           ▼
 [ Reader ➔ Processor ➔ Writer ] ───➔ [ PostgreSQL (Batch Metadata & Custom Log) ]
           │
           ▼ (If Failure Occurs)
 ┌───────────────────────────────────┐
 │      Notification Dispatcher      │
 └─────────────────┬─────────────────┘
                   │ (Strategy Pattern Async Routing)
         ┌─────────┼─────────┐
         ▼         ▼         ▼
     [ Slack ]  [ Mail ] [ Kakao ]


🛠 Tech Stack
💻 Backend Core
Language: Java 17

Framework: Spring Boot 3.x, Spring Batch 5.x, Spring Data JPA

Validation & Scheduling: Spring Validation, Spring Scheduler

𝌚 Data & Infrastructure
Database: PostgreSQL (Metatable & Business Log)

Container: Docker (Local Infrastructure Sandbox)

Build System: Gradle

IDE: IntelliJ IDEA

📁 Project Structure
