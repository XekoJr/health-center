# Health Center — Programming Final Project (LEI, ESTGC-Coimbra)

A text-based Java application (no GUI), built with Object-Oriented Programming, to manage a **medical analysis clinic**. Final project for the **Programação** course (LEI, 1st semester, 2025/2026 academic year, ESTGC — Polytechnic Institute of Coimbra), Periodic Assessment 3 / Exam Assessment.

The full assignment brief is in [`LEI_Prog_AP3-Enunciado 2025_12_02-1.pdf`](<LEI_Prog_AP3-Enunciado%202025_12_02-1.pdf>). [`checklist.html`](checklist.html) tracks progress against the requirements (R1–R83) described in the brief.

## What the application does

- **Users**: registration/authentication for administrators, technicians and clients, each with their own permissions.
- **Services**: a client requests a lab analysis service → the administrator approves/rejects it and assigns a responsible technician → the technician carries it out → the service is completed. States: `started → approved/rejected → in progress → finished`.
- **Lab analyses**: code, certification, lab, responsible technician, required chemical components, associated medical areas and tests.
- **Supporting catalog**: chemical components (with stock), suppliers, orders, medical areas, certifications.
- **File persistence**: credentials in `credenciais_acesso.txt`, application data serialized in `dados_apl.dat`, action log in `log.txt`, run counters in `info_sistema.dat`, service export to CSV.

## Structure

```
src/
├── App.java              # entry point
├── app/                   # startup, menus, session (ApplicationManager, MenuManager, Session)
├── users/                 # User, Admin, Client, Technician, ManageUsers
├── services/              # Service, LabAnalysis, Test, Category, Certification,
│                          # ChemicalComponent, Supplier, Order, MedicalArea, ManageCatalog, ManageServices
├── data/                  # persistence (AppData, CredentialsManager, DataStorage)
└── util/                  # Validator, LogManager, SystemInfo, Colors
```

## Running it

Plain Java project (no build tool) — compile and run `App.java` from `src/` with the JDK, or from VS Code (Java extension, `.vscode/settings.json` already configured).

```bash
cd src
javac -d ../out $(find . -name "*.java")
java -cp ../out App
```

On startup, the app reads `dados_apl.dat` (if present) and credentials from `credenciais_acesso.txt`; on shutdown, it saves state automatically.

## Note on authorship

Per the assignment brief, all code and the final report must be written exclusively by the student — **no AI or code-generation tools are allowed** (Copilot, ChatGPT, IDE autocomplete, etc.), on penalty of grade cancellation and an academic fraud case.
