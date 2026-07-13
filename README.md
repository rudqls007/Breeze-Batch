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

<pre><code>
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
</code></pre>

---

## 🛠 Tech Stack

<details>
<summary>💻 Backend Core (클릭하여 열기)</summary>

* **Language:** Java 17
* **Framework:** Spring Boot 3.x, Spring Batch 5.x, Spring Data JPA
* **Validation & Scheduling:** Spring Validation, Spring Scheduler
</details>

<details>
<summary>𝌚 Data & Infrastructure (클릭하여 열기)</summary>

* **Database:** PostgreSQL (Metatable & Business Log)
* **Container:** Docker (Local Infrastructure Sandbox)
* **Build System:** Gradle
* **IDE:** IntelliJ IDEA
</details>

---

## 📁 Project Structure

<pre><code>
com.example.breezebatch
├── admin         # 배치 운영 및 제어 핵심 서비스 (Restart, Management Logic)
├── config        # 인프라 설정 (Spring Batch, DB Data Source, Lock Manager)
├── controller    # 외부 오케스트레이션을 위한 REST Admin API Layer
├── domain        # 비즈니스 도메인 및 실행 로그 엔티티
├── dto           # 외부 레이어 인터페이스용 Data Transfer Object
├── job           # 실제 배치 Job 명세 레이어
│   ├── daily     # 일단위 배치 (Daily Statistics Sync Job)
│   ├── weekly    # 주간단위 배치 (Weekly Aggregate Job)
│   └── monthly   # 월단위 배치 (Monthly Settlements Job)
├── listener      # 배치 실행 모니터링을 위한 Job / Step Execution Listener
├── notification  # 알림 디스패처 엔진 및 채널별 모듈 (Slack, Mail, Kakao)
└── repository    # 영속성 데이터 접근 레이어 (JPA Repositories)
</code></pre>

---

## 🔄 Core Mechanics

### 🎯 Batch Flow & Notification Pipeline
배치 수행 주기 스케줄링부터 실패 시 채널별 라우팅까지의 단방향 데이터 파이프라인입니다.

<pre><code>
[ Scheduler ] ➔ [ JobLauncher ] ➔ [ Spring Batch Job ] ➔ [ Step ]
                                                            │
  ┌─────────────────────────────────────────────────────────┘
  ▼
[ Reader ] ➔ [ Processor ] ➔ [ Writer ] ➔ [ Batch DB Custom Logging ]
                                                            │ (If Exception)
                                                            ▼
                                                [ BatchFailureListener ]
                                                            │
                                                            ▼
                                                [ NotificationDispatcher ]
                                                            │ (Async Broadcaster)
                                                ┌───────────┼───────────┐
                                                ▼           ▼           ▼
                                            [ Mail ]    [ Slack ]   [ Kakao ]
</code></pre>

### 🔁 Self-Healing (Restart Workflow)
메타데이터 식별자를 추적하여 중단된 지점부터 배치를 안전하게 이어 나가는 흐름입니다.

<pre><code>
[ Batch Failed Status ] ➔ [ Admin REST Client API Call ]
                                 │
                                 ▼
                     [ JobExplorer Meta Search ] ➔ [ JobOperator.restart() ]
                                                            │
                                                            ▼
                                                    [ Step Resumption ]
                                                            │
                                                            ▼
                                                  [ Success Notification ]
</code></pre>

---

## 📈 Batch Monitoring & Log Matrix

운영 시 디버깅 편의성을 극대화하기 위해 Spring Batch 기본 메타데이터 테이블 외에 아래 항목을 포함하는 **커스텀 데이터베이스 로깅 레이어**를 분리 운영합니다.

* **Job Matrix:** `Job Name`, `Job Execution ID`, `Job Parameters`
* **Performance Matrix:** `Start Time`, `End Time`, `Duration (ms)` (성능 병목지점 판별용)
* **Context Matrix:** `Status`, `Exit Message`, `Error StackTrace` (텍스트 전체 본문 저장)

---

## 📊 REST Admin API Specification

외부 스케줄러(Jenkins, Airflow 등) 및 사내 어드민 페이지와의 연동을 위해 RESTful 표준 API를 제공합니다.

| Method | End Point | Description | Request Payload / Parameter | Response Sample (`200 OK`) |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/batch/run` | 특정 Job 수동 즉시 실행 | `{"jobName": "dailyStatisticJob", "params": {"date": "2026-07-13"}}` | `{"status": "COMPLETED", "executionId": 12}` |
| `POST` | `/batch/restart` | 실패한 Job 컴포넌트 재시작 | `{"failedExecutionId": 12}` | `{"status": "RUNNING", "newExecutionId": 13}` |
| `GET` | `/batch/status` | 특정 배치의 최종 실행 상태 조회 | `?jobName=dailyStatisticJob` | `{"jobName": "...", "lastStatus": "FAILED"}` |
| `GET` | `/batch/logs` | 커스텀 실행 로그 목록 검색 | `?page=0&size=10&status=FAILED` | `{"content": [...], "totalElements": 1}` |

---

## 📝 Troubleshooting & Production Deep Dive

### 💥 Issue 1: 다중 인스턴스 환경에서 스케줄러 중복 실행 (Race Condition)
* **상황 (Context):** 배치 어플리케이션을 다중 서버(Scale-Out)로 구성하자, 각 인스턴스의 `Spring Scheduler`가 동일한 시간에 각자 배치를 띄워 데이터 중복 적재 및 메타테이블 데드락 발생.
* **원인 (Root Cause):** Spring 내장 `ThreadPoolTaskScheduler`는 인스턴스 간 상태를 공유하지 않는 분산 환경 인지 불능 상태였음.
* **해결 (Resolution):** `Distributed Lock` 아키텍처를 도입하여 해결. 배치 실행 진입점에 데이터베이스 기반 네임드 락 또는 분산 락 컴포넌트를 태워 먼저 락을 획득한 하나의 인스턴스만 배치를 수행하고, 락 획득에 실패한 타 서버는 실행을 안전하게 스킵하도록 방어 로직 구축.

### 💥 Issue 2: Spring Batch 5 마이그레이션 시 파라미터 중복 실행 제한 예외
* **상황 (Context):** `JobInstanceAlreadyCompleteException` 발생으로 인해 실패 데이터 수정 후 재시작 시 배치가 돌아가지 않는 현상.
* **원인 (Root Cause):** Spring Batch 5부터 `JobParameters`를 구성할 때 모든 파라미터가 기본적으로 식별자(`Identifying`)로 취급되어, 동일 파라미터로 두 번 이상 실행할 수 없도록 강제됨.
* **해결 (Resolution):** `JobParameterBuilder` 설정 시 고유 식별이 필요 없는 날짜 파라미터 등은 `.addJobParameter(..., false)` 형태로 비식별 속성을 명시하여 중복 파라미터 전달 규칙을 재정의함.

### 💥 Issue 3: Chunk-size 대용량 처리 시 Connection Timeout 발생
* **상황 (Context):** 청크 단위 대용량 쓰기(`Writer`) 도중 외부 API 호출이나 무거운 트랜잭션이 물리면서 `HikariPool-Connection Not Available` 예외와 함께 배치 비정상 종료.
* **원인 (Root Cause):** Step 트랜잭션 주기가 필요 이상으로 길어 Chunk가 처리되는 동안 커넥션이 풀에 반환되지 못하고 고갈됨.
* **해결 (Resolution):** 배치 전용 데이터 소스 설정을 분리하고 `HikariCP` 최적화를 수행함. Chunk 단위를 비즈니스 메모리 한계선(예: 1,000건)에 맞춰 재조정하고, 트랜잭션 타임아웃 주기를 명시적으로 지정하여 커넥션 점유 시간을 최소화함.

---

## 🚀 Future Roadmap

- [ ] **Redisson 기반 분산 락(Distributed Lock) 고도화:** DB 부하를 줄이기 위한 Redis 인메모리 락 전환
- [ ] **Observability 확장:** `Prometheus` 및 `Grafana` 대시보드 연동을 통한 실시간 시스템 메트릭 계측
- [ ] **OpenTelemetry 추적:** 외부 API 송수신 구간 레이턴시 분석을 위한 분산 트레이싱 적용
- [ ] **Cloud Native Scheduling:** 24시간 상주형 스케줄러에서 `Kubernetes CronJob` 기반의 서버리스 단발성 배치 아키텍처로 점진적 전환
- [ ] **AI-powered Failure Analysis:** 배치 실패 리스너에 `OpenAI API`를 연동, 에러 스택 트레이스를 실시간 분석하여 조치 가이드라인 문서와 함께 알림을 발송하는 지능형 모니터링 도입
