window.onload = () => {
    const ui = SwaggerUIBundle({
        url: "/v1/api-docs",
        dom_id: "#swagger-ui",
        deepLinking: true,
        displayRequestDuration: true,
        filter: true,
        persistAuthorization: true,
        tryItOutEnabled: true,
        docExpansion: "none",
        defaultModelsExpandDepth: 1,
        defaultModelExpandDepth: 1,
        presets: [
            SwaggerUIBundle.presets.apis,
            SwaggerUIStandalonePreset
        ],
        plugins: [
            SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "BaseLayout",
        onComplete: () => {
            buildTopActions();
            bindUtilityActions();
            bindSidebarToggle();
            syncTitle();
        }
    });

    window.ui = ui;
};

function buildTopActions() {
    const target = document.getElementById("hero-actions");
    if (!target) return;

    target.innerHTML = `
        <button class="portal-btn" id="btn-auth">Authorize</button>
        <button class="portal-btn portal-btn--ghost" id="btn-expand">Expand all</button>
        <button class="portal-btn portal-btn--ghost" id="btn-collapse">Collapse all</button>
        <button class="portal-btn portal-btn--ghost" id="btn-focus-filter">Search</button>
    `;
}

function bindUtilityActions() {
    const authBtn = document.getElementById("btn-auth");
    const expandBtn = document.getElementById("btn-expand");
    const collapseBtn = document.getElementById("btn-collapse");
    const filterBtn = document.getElementById("btn-focus-filter");

    authBtn?.addEventListener("click", () => {
        const realAuthBtn = document.querySelector(".swagger-ui .auth-wrapper .authorize");
        realAuthBtn?.click();
    });

    expandBtn?.addEventListener("click", () => {
        document.querySelectorAll(".swagger-ui .opblock-tag").forEach(tag => {
            const expanded = tag.getAttribute("aria-expanded");
            if (expanded !== "true") tag.click();
        });

        setTimeout(() => {
            document.querySelectorAll(".swagger-ui .opblock .opblock-summary").forEach(summary => {
                const block = summary.closest(".opblock");
                if (block && !block.classList.contains("is-open")) {
                    summary.click();
                }
            });
        }, 150);
    });

    collapseBtn?.addEventListener("click", () => {
        document.querySelectorAll(".swagger-ui .opblock.is-open .opblock-summary").forEach(summary => {
            summary.click();
        });

        setTimeout(() => {
            document.querySelectorAll(".swagger-ui .opblock-tag").forEach(tag => {
                const expanded = tag.getAttribute("aria-expanded");
                if (expanded === "true") tag.click();
            });
        }, 100);
    });

    filterBtn?.addEventListener("click", () => {
        const filterInput = document.querySelector(".swagger-ui .filter input");
        filterInput?.focus();
    });
}

function syncTitle() {
    const infoTitle = document.querySelector(".swagger-ui .info .title");
    if (infoTitle) {
        document.title = `${infoTitle.textContent.trim()} | MemoryMe Docs`;
    }
}

function bindSidebarToggle() {
    const toggleBtn = document.getElementById("sidebarToggle");
    const sidebar = document.getElementById("sidebar");

    if (!toggleBtn || !sidebar) return;

    toggleBtn.addEventListener("click", () => {
        const isOpen = sidebar.classList.toggle("open");
        toggleBtn.setAttribute("aria-expanded", String(isOpen));
        toggleBtn.textContent = isOpen ? "✕" : "☰";
    });

    document.addEventListener("click", (event) => {
        const isMobile = window.innerWidth <= 1180;
        if (!isMobile) return;

        const clickedInsideSidebar = sidebar.contains(event.target);
        const clickedToggle = toggleBtn.contains(event.target);

        if (!clickedInsideSidebar && !clickedToggle) {
            sidebar.classList.remove("open");
            toggleBtn.setAttribute("aria-expanded", "false");
            toggleBtn.textContent = "☰";
        }
    });

    window.addEventListener("resize", () => {
        if (window.innerWidth > 1180) {
            sidebar.classList.remove("open");
            toggleBtn.setAttribute("aria-expanded", "false");
            toggleBtn.textContent = "☰";
        }
    });
}