# TRE

# TRE (Transaction Risk Exposure) Documentation Wiki

![image.png](TRE/image.png)

> ℹ️ **About This Document**
> 
> 
> This page serves as the architectural and domain source of truth for the **TRE (Transaction Risk Exposure)** system.
> 
> It is designed to rapidly onboard new engineering and product team members to the system's mission, technical components, and the core domain logic of ACH risk management.
> 

The TRE (Transaction Risk Exposure) team acts as the automated "safety net" for the bank's electronic money transfers.

Before a business can send out massive batches of payments (like payroll) or pull money from customers, the TRE system instantly checks if that business has enough actual cash or approved credit to cover the total amount. Essentially, they function as a high-speed financial bouncer—stopping risky transactions in their tracks to ensure the bank doesn't accidentally lose millions of dollars.

---

## 1. System Mission & Business Context

**The Core Mission**

The TRE (Transaction Risk Exposure) application is the critical risk gateway for Automated Clearing House (ACH) transaction processing.

Its primary mandate is to evaluate, approve, or suspend ACH batch files based on real-time and multi-day exposure limits before they are released to the Federal Reserve.

**Business Impact & Scale**

- **Customer Base:** Manages risk exposure for approximately **45,000 customers**
- **Financial Protection:** Prevents massive financial loss by ensuring customers (like "ABC Trucking" or "A-Plus Automotive") do not transmit funds exceeding their approved credit thresholds or current account balances.
- **Automation Necessity:**
    - Processing at this scale requires highly automated, low-latency decisioning.
    - While standard transactions are evaluated via matrix logic and MQ queues instantaneously, exceptions trigger automated "Risk Alerts" for manual intervention by Responsible Credit Officers and ACH Operations.

---

## 2. Architecture Analysis Template

*(Engineers can use this framework to document specific microservices or transaction paths within TRE.)*

### A. Data Flow & Routing

1. **Ingestion:** Customers submit Direct Deposit/Payment files to **WF ACH**.
2. **Approval Request:** WF ACH sends an MQ request (`NA.TRE.BATCH.REQUEST`) to TRE.
3. **Evaluation:** TRE queries limits, current multi-day exposure, and customer grades.
    - *If Prepaid:* TRE also makes an HTTP call to **TMS** to check balances and memo post the debit.
4. **Decision:**
    - **Approve:** TRE replies via MQ (`NA.TRE.BATCH.REPLY`) and logs the exposure. The file moves to the **ACH Warehouse** for Federal Reserve routing.
    - **Pend/Decline:** TRE returns a PEND status, creates a Risk Alert, and triggers an event message via MQ (`TRE.SMU.NOTIFYS`) to the customer and via SMTP to Credit Officers.

### B. Component Breakdown

- **TRE Engine:** The core decisioning matrix that calculates limits vs. requested batch totals.
- **TRE Matrix:** The rules engine applied when a transaction goes over a standard limit.
- **TMS (Treasury Management System):** Interfaces with TRE for real-time balance checks and memo-debiting (specifically for Prepaid customers).
- **Event Messaging Engine:** Handles outbound communications to customers (e.g., insufficient funds notifications) and internal alerts.

### C. Integrations & Data Sources

- **Underwriting/Credit:** Receives daily NDM files from **AFS** (Customer Grade) and underwriting systems (**CATools, Credit View, Athena Blast**).
- **Reference Data:** Integrated with **Hogan CIS** (Legal Name, Tax ID) and **MCV** (Tax ID grouping, AU groupings).
- **Reporting:** Data flows from TRE, Hogan, and MCV into **Oracle PNL2**, then to **SQLServer**, culminating in **SSRS** (SQL Server Reporting Services) for Customer Exposure and Risk Alert History pages.

---

## 3. ACH Risk Domain Guide

Understanding TRE requires understanding the financial risk inherent in ACH timing.

ACH is not a real-time money movement network; it relies on **settlement dates** and **multi-day processing windows**.

### Terminology Table

| **Term** | **Definition** |
| --- | --- |
| **ACH Exposure** | The total dollar amount of unsettled, in-flight ACH transactions a customer has pending at any given moment. |
| **ACH Limits** | The maximum allowable exposure for a specific transaction type (e.g., $1,000,000).
Limits are segregated by type (ACHC, ACHP, ACHD). |
| **ACHC (Credit Qualified)** | Transactions where the customer is *sending* money (e.g., Payroll).

Risk: The bank sends the money to the Fed, but the customer's account doesn't have the funds to cover it on settlement day. |
| **ACHP (Prepaid Credit)** | Like ACHC, but the customer must have the cash in their account *right now*. TRE checks the balance and memo-debits the account immediately. |
| **ACHD (Debit Qualified)** | Transactions where the customer is *pulling* money from external accounts.

Risk: The pulled funds are returned (e.g., insufficient funds at the external bank) *after* the customer has already spent the money credited to them. |
| **Settlement Date** | The future date when funds actually move between the banks at the Federal Reserve. |

### The Logic of Limits: Multi-Day Exposure Calculation

A critical concept in TRE is that exposure **stacks across days**. Limits are not evaluated on a per-file basis, but on a rolling timeline.

**Example Calculation:** If a customer submits three $25,000 files with overlapping settlement windows, their exposure aggregates on the overlapping days.

|  | **Day 1** | **Day 2** | **Day 3** | **Day 4** | **Day 5** |
| --- | --- | --- | --- | --- | --- |
| **File #1** | $25,000 | $25,000 | $25,000 |  |  |
| **File #2** |  | $25,000 | $25,000 | $25,000 |  |
| **File #3** |  |  | $25,000 | $25,000 | $25,000 |
| **Total Exposure** | **$25,000** | **$50,000** | **$75,000** | **$50,000** | **$25,000** |

**TRE ensures that the *Total Exposure* on any given day never exceeds the customer's global ACH limit.**

---

## 4. Operational Workflows & Alert Mechanics

### Limit Setup & Underwriting

Limits are not created within TRE; they are enforced by it.

1. **Review:** Credit Officers use LOB-specific tools (CATools, Credit View, Athena Blast) to underwrite the customer.
2. **Approval:** Limits are approved in **TMSAC** by Credit Officers.
3. **Ingestion:** Approved limits are currently entered manually into TRE by the TRE Support Team.

### Risk Alert Resolution

When a file violates a rule (e.g., over limit, insufficient funds for ACHP), it enters a **PEND** status and creates a Risk Alert.

1. Bankers receive an MQ/SMTP notification (`TRE.EMAIL.REQUEST`).
2. Responsible Credit Officers review the alert within the TRE interface.
3. If approved by the Credit Officer, ACH Operations is called to formally Approve/Decline the file within TRE, which then releases the MQ reply back to the ACH engine.

---

## 5. Notion "Wiki" Components

### Glossary of Technical Acronyms

- **MQ (Message Queue):** IBM MQ used for asynchronous, reliable communication between TRE and ACH/Messaging systems.
- **NDM (Network Data Mover):** A file transfer protocol used to ingest daily batch files (grades, limits) from underwriting systems.
- **TMS (Treasury Management System):** Used for real-time account interactions.
- **SSRS (SQL Server Reporting Services):** The BI tool used to visualize exposure and risk history.

### Critical Domain Notes

> 💡 **Critical Logic: Credit vs. Debit Exposure Logging**
> 
> 
> Notice the difference in *when* exposure is logged based on transaction type:
> 
> - **Credits (ACHC):** Exposure is logged *prior to and up to* the settlement date (e.g., Sent 5/29, Settled 6/3. Exposure = 5/30, 5/31, 6/3). This is because the risk ends once the settlement is successfully funded.
> - **Debits (ACHD):** Exposure is logged *on and after* the settlement date (e.g., Sent 5/29, Settled 6/3. Exposure = 6/3, 6/4, 6/5). This accounts for the **Return Window**—the days after settlement where the external bank might reject and return the debit.

> ⚠️ **Common Pitfall: The Prepaid Fallacy**
> 
> 
> Do not assume Prepaid (ACHP) files bypass exposure limits just because they are funded upfront.
> TRE still tracks their exposure limit AND actively forces a balance check/memo-debit via TMS over HTTP.
> If the TMS check fails (Insufficient Funds), an immediate Event Message is dispatched to the customer.
>