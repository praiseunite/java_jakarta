# 00 — Environment Setup Guide
## Jakarta Enterprise Application Development
### Complete Step-by-Step Installation & Configuration Manual

---

## Before You Begin — Read This First

Think of setting up your development environment like building a kitchen before you start cooking. You need the right appliances, the right tools, and the right ingredients before a single dish can be made. In software development, your "kitchen" is your development environment — the software tools you install on your computer that allow you to write, test, and run enterprise Java applications.

This guide walks you through **every single step** of the setup process. Do not skip any step. Each step builds on the one before it.

---

## 1. Computer Requirements (Hardware)

Before installing any software, make sure your computer meets these minimum requirements. If your machine is below these specs, you may experience slowness, crashes, or errors during development.

| Requirement | Minimum | Recommended |
|---|---|---|
| **RAM (Memory)** | 8 GB | 16 GB or more |
| **Processor (CPU)** | Intel Core i5 / AMD Ryzen 5 (64-bit) | Intel Core i7 / AMD Ryzen 7 or better |
| **Storage (Hard Drive)** | 20 GB free space | 50 GB free (SSD preferred) |
| **Operating System** | Windows 10 (64-bit), macOS 11+, Ubuntu 20.04+ | Windows 11 (64-bit) |
| **Internet Connection** | Required for downloads | Stable broadband |
| **Screen Resolution** | 1366 × 768 | 1920 × 1080 (Full HD) |

> ⚠️ **IMPORTANT**: You **must** use a 64-bit operating system. Jakarta EE application servers do not run on 32-bit systems.

---

## 2. Software You Will Install

Here is a complete list of everything you need, in the order you will install it:

| # | Software | Version Required | Purpose | Free? |
|---|---|---|---|---|
| 1 | **Java JDK** | JDK 17 LTS or JDK 21 LTS | The core Java runtime to compile and run Java programs | ✅ Yes |
| 2 | **Eclipse IDE** | 2022-09 (Enterprise Edition) | The code editor (IDE) where you write your programs | ✅ Yes |
| 3 | **WildFly Server** | WildFly 26.1.2 Final or 30+ | The application server that runs your enterprise beans | ✅ Yes |
| 4 | **Maven** | 3.9+ | Build tool (comes bundled with Eclipse) | ✅ Yes |
| 5 | **PostgreSQL** | 16+ | The database for storing application data | ✅ Yes |
| 6 | **DBeaver** | Latest | Database management GUI tool | ✅ Yes |

---

## 3. Step-by-Step Installation

### STEP 1 — Download and Install Java JDK

#### What is JDK and Why Do You Need It?

The **Java Development Kit (JDK)** is the foundational software that makes everything else work. Think of it like electricity — without it, nothing turns on. The JDK includes:
- **Java Compiler (`javac`)**: Converts your Java code into bytecode that the computer understands
- **Java Runtime Environment (JRE)**: Runs your compiled Java programs
- **Development tools**: Debugging tools, documentation tools, etc.

Jakarta EE requires **JDK 11 or higher**. We will use **JDK 17 LTS** (Long-Term Support) because it is stable, widely supported, and fully compatible with Jakarta EE 9 and 10.

#### Download Steps

1. Open your web browser and go to: **https://www.oracle.com/java/technologies/downloads/**
2. Select the **Java 17** tab
3. Select your operating system:
   - **Windows**: Choose `x64 Installer` (the `.exe` file) — file size is approximately 152 MB
   - **macOS**: Choose `x64 DMG Installer`
   - **Linux**: Choose `x64 Compressed Archive`

> 📷 *Reference: Figure 1.2 in the textbook shows the Java SE Development Kit 19 downloads page. The process is identical for JDK 17 — just make sure you select Java 17.*

#### Installation Steps (Windows)

1. Double-click the downloaded `.exe` file
2. Click **Yes** if Windows asks "Do you want to allow this app to make changes?"
3. The JDK Setup Wizard opens — click **Next**
4. Leave the installation directory as the default (e.g., `C:\Program Files\Java\jdk-17`) — click **Next**
5. Wait for the installation to complete (1–2 minutes)
6. Click **Close** when finished

#### Verify the Installation (CRITICAL — Do This!)

After installing, you must verify that Java was installed correctly. Here is how:

1. Press `Windows Key + R`, type `cmd`, and press **Enter** to open the Command Prompt
2. Type the following command exactly and press **Enter**:
   ```
   java -version
   ```
3. You should see output like this:
   ```
   java version "17.0.x" 2023-xx-xx LTS
   Java(TM) SE Runtime Environment (build 17.0.x+xx-LTS)
   Java HotSpot(TM) 64-Bit Server VM (build 17.0.x+xx-LTS, mixed mode, sharing)
   ```

> ✅ **If you see the above output, Java is installed correctly. Proceed to Step 2.**
>
> ❌ **If you see `'java' is not recognized as an internal or external command`**, then Java's PATH was not set correctly. Follow the JAVA_HOME setup below.

#### Setting JAVA_HOME (If Needed — Windows)

`JAVA_HOME` is an environment variable that tells your computer and other tools (like Eclipse and Maven) where Java is installed.

1. Press `Windows Key`, search for **"Environment Variables"**, and click **"Edit the system environment variables"**
2. In the System Properties window, click **"Environment Variables..."**
3. Under **System variables**, click **New**:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-17` (or wherever Java was installed)
4. Find the `Path` variable in **System variables**, click **Edit**
5. Click **New** and add: `%JAVA_HOME%\bin`
6. Click **OK** on all windows
7. Close and reopen Command Prompt, then run `java -version` again

---

### STEP 2 — Download and Install Eclipse IDE

#### What is Eclipse IDE and Why This Version?

An **Integrated Development Environment (IDE)** is your main workspace — it is where you write code, manage files, configure your server, and run your application. Think of it like a fully equipped workshop: it has the workbench, the tools, the drawers, and the instructions all in one place.

We use **Eclipse IDE for Enterprise Java and Web Developers (2022-09)** because it comes pre-configured with:
- Jakarta EE project templates
- WildFly server integration
- XML editors for configuration files
- Web application project support

> ⚠️ Do NOT install the basic Eclipse IDE or Eclipse IDE for Java Developers. You must install **"Eclipse IDE for Enterprise Java and Web Developers"** specifically.

#### Download Steps

1. Open your browser and go to: **https://www.eclipse.org/downloads/packages/**
2. On the page, look for the **"Eclipse IDE for Enterprise Java and Web Developers"** package
3. Select your operating system (Windows, macOS, or Linux)
4. Click the **Download** button (the file is approximately 400–500 MB)

> 📷 *Reference: Figure 1.1 in the textbook shows the Eclipse IDE 2022-09 Packages page with "Eclipse IDE for Enterprise Java and Web Developers" highlighted.*

#### Installation Steps (Windows)

1. The downloaded file is a `.zip` archive. Right-click it and select **Extract All**
2. Choose a destination folder (e.g., `C:\eclipse`) and click **Extract**
3. Open the extracted folder and find `eclipse.exe`
4. **Right-click** `eclipse.exe` and select **"Create shortcut"**, then move the shortcut to your Desktop
5. Double-click the shortcut to launch Eclipse for the first time
6. Eclipse will ask you to choose a **Workspace** — this is the folder where all your projects will be saved
   - Suggested path: `C:\Users\YourName\eclipse-workspace`
   - Click **Launch**
7. Eclipse will open to the **Welcome** screen — click **"Open IDE"** or close the Welcome tab

> ✅ **Eclipse is now installed. You should see the Eclipse workbench (main window).**

---

### STEP 3 — Install WildFly Application Server

#### What is WildFly and Why Do We Need It?

Imagine you have written a recipe (your Java enterprise application). You need a **kitchen** (the application server) to actually cook the food. The application server provides all the services your enterprise beans need — transaction management, security, connection pooling, messaging — things you would otherwise have to write yourself.

**WildFly** (previously called JBoss Application Server) is a free, open-source, Jakarta EE-certified application server. It is one of the most popular choices for enterprise Java development worldwide. We install it directly inside Eclipse so that you can start and stop the server without leaving your IDE.

> ⚠️ **Version Requirement**: You must use **WildFly 24 or higher** for Jakarta EE 9 compatibility. We will install **WildFly 26.1.2 Final** as shown in the textbook.

#### Step 3a — Open the Server Configuration in Eclipse

1. At the bottom of the Eclipse window, look for the **"Servers"** tab
2. If you do not see the Servers tab:
   - Go to menu: `Window` → `Show View` → `Servers`
3. In the Servers tab, you will see the message: **"No servers are available. Click this link to create a new server"**
4. Click on that link

> 📷 *Reference: Figure 1.3 in the textbook shows the Eclipse IDE with the "No servers are available. Click this link to create a new server" message at the bottom.*

#### Step 3b — Select WildFly as the Server Type

1. The **"Define a New Server"** wizard opens
2. In the search box that says "type filter text", type: `wildfly`
3. You will see a list of WildFly versions appear
4. Select **"WildFly 24+"** (this option works for WildFly 24 and above)
5. Leave the **Server's host name** as `localhost`
6. Leave the **Server name** as `WildFly 24+`
7. Click **Next**

> 📷 *Reference: Figure 1.4 in the textbook shows the "Define a New Server" dialog with WildFly 24+ selected.*

#### Step 3c — Configure the Server Adapter

1. The next screen is the **"WildFly Application Server 24+"** configuration page
2. Leave all settings at their default values:
   - Controlled by: `Local`
   - Server lifecycle is externally managed: unchecked
   - Assign a runtime to this server: checked
3. Click **Next**

> 📷 *Reference: Figure 1.5 in the textbook shows the "Installing Server Adapter" screen.*

#### Step 3d — Download WildFly Runtime

1. The **"JBoss Runtime"** screen appears
2. Since WildFly is not yet downloaded, click **"Download and install runtime..."** (top right link)

> 📷 *Reference: Figure 1.6 in the textbook shows the Downloading WildFly 24+ and Installing Runtime screen.*

#### Step 3e — Select the WildFly Version

1. A **"Download Runtimes"** dialog appears showing all available WildFly versions
2. Scroll down and select **"WildFly 26.1.2.Final"**
3. Click **Next**

> 📷 *Reference: Figure 1.7 in the textbook shows the Download Runtimes window with WildFly 26.1.2 Final highlighted.*

#### Step 3f — Accept the License Agreement

1. The **WildFly 26.1.2.Final** license agreement is displayed
2. Read the license text (it is the GNU Lesser General Public License)
3. Select **"I accept the terms of the license agreement"**
4. Click **Next**

> 📷 *Reference: Figure 1.8 in the textbook shows the Accept Terms and Conditions dialog.*

#### Step 3g — Complete the Setup

1. The download will begin automatically (WildFly is approximately 200 MB)
2. Wait for the download to complete — a progress bar will show the download status
3. Once complete, click **Finish**

> 📷 *Reference: Figure 1.9 in the textbook shows the Complete WildFly 26.1.2 Server Setup screen.*

#### Verify WildFly is Working

1. In the **Servers** tab at the bottom of Eclipse, you should now see **"WildFly 26.1.2.Final"** listed
2. Right-click on the server name
3. Select **"Start"**
4. Watch the **Console** tab at the bottom — you should see WildFly starting up
5. When you see the line:
   ```
   WildFly Full 26.1.2.Final (WildFly Core 18.1.1.Final) started in XXXX ms
   ```
   WildFly is running successfully.
6. Open your browser and go to: **http://localhost:8080**
7. You should see the **WildFly Welcome Page**

> ✅ **Congratulations! Your development environment is fully set up.**

---

### STEP 4 — Install PostgreSQL Database (For Later Sessions)

We will use PostgreSQL as our database starting from Session 3. Install it now so it is ready when needed.

1. Go to: **https://www.postgresql.org/download/**
2. Select your operating system
3. Download **PostgreSQL 16**
4. Run the installer:
   - Leave the installation directory as default
   - Select components: PostgreSQL Server, pgAdmin 4, Stack Builder, Command Line Tools
   - Choose a data directory (default is fine)
   - Set a **password for the postgres user** — **write this password down and do not lose it**
   - Port: Leave as `5432`
   - Click **Next** through the remaining screens
   - Click **Finish**

---

## 4. Quick Reference — What Each Tool Does

```
YOUR COMPUTER
│
├── JDK 17 ────────────────── Compiles and runs Java code
│
├── Eclipse IDE ────────────── Where you write your code
│   └── Maven (built-in) ──── Manages project dependencies (JAR files)
│
├── WildFly Server ─────────── Runs your enterprise application
│   └── EJB Container ──────── Manages your Enterprise Beans
│
└── PostgreSQL ─────────────── Stores your application's data
```

---

## 5. Common Setup Mistakes and How to Fix Them

| Problem | Cause | Fix |
|---|---|---|
| `java -version` not recognized | JAVA_HOME not set or JDK not in PATH | Set JAVA_HOME and add `%JAVA_HOME%\bin` to PATH |
| Eclipse won't open | Wrong Eclipse package downloaded | Make sure you downloaded "Enterprise Java and Web Developers" edition |
| WildFly won't start | Port 8080 already in use by another program | Stop Skype, IIS, or other services using port 8080 |
| WildFly download fails | Slow internet or firewall blocking | Try downloading WildFly manually from wildfly.org and pointing Eclipse to the extracted folder |
| "No Java virtual machine found" when starting Eclipse | JDK not properly installed | Reinstall JDK and restart your computer |

---

## 6. Summary Checklist

Before moving to Session 1, confirm all items below are ✅:

- [ ] Computer has at least 8GB RAM and 20GB free storage
- [ ] JDK 17 installed and `java -version` works in Command Prompt
- [ ] JAVA_HOME environment variable is set
- [ ] Eclipse IDE for Enterprise Java and Web Developers (2022-09) is installed
- [ ] Eclipse opens successfully and shows the workbench
- [ ] WildFly 26.1.2 Final is installed inside Eclipse
- [ ] WildFly starts and http://localhost:8080 shows the WildFly welcome page
- [ ] PostgreSQL installed (will be configured in Session 3)

**You are now ready to build enterprise Java applications. Let's go!** 🚀
