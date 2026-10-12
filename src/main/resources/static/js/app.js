// SaludSync - comportamiento común de la interfaz
(function () {
    'use strict';

    // ---------- Menú lateral (móvil) ----------
    const sidebar = document.getElementById('sidebar');
    const backdrop = document.querySelector('.sidebar-backdrop');
    document.querySelectorAll('[data-sidebar-toggle]').forEach(function (el) {
        el.addEventListener('click', function () {
            sidebar.classList.toggle('open');
            backdrop.classList.toggle('open');
        });
    });

    // ---------- Modales ----------
    let ultimoFoco = null;

    function abrirModal(modal) {
        ultimoFoco = document.activeElement;
        modal.classList.add('open');
        document.body.style.overflow = 'hidden';
        const foco = modal.querySelector('[data-autofocus]') || modal.querySelector('.btn');
        if (foco) foco.focus();
    }

    function cerrarModal(modal) {
        modal.classList.remove('open');
        document.body.style.overflow = '';
        if (ultimoFoco) ultimoFoco.focus();
    }

    document.querySelectorAll('.modal').forEach(function (modal) {
        modal.addEventListener('click', function (e) {
            if (e.target === modal || e.target.closest('[data-modal-close]')) cerrarModal(modal);
        });
    });

    document.addEventListener('keydown', function (e) {
        if (e.key !== 'Escape') return;
        document.querySelectorAll('.modal.open').forEach(cerrarModal);
        if (sidebar && sidebar.classList.contains('open')) {
            sidebar.classList.remove('open');
            backdrop.classList.remove('open');
        }
    });

    window.SaludSync = { abrirModal: abrirModal, cerrarModal: cerrarModal };

    // ---------- Confirmación de acciones destructivas ----------
    // Uso: <a href="..." data-confirm="Texto" data-confirm-title="Título" data-confirm-ok="Eliminar">
    const confirmModal = document.getElementById('confirm-modal');
    let accionPendiente = null;

    document.addEventListener('click', function (e) {
        const trigger = e.target.closest('[data-confirm]');
        if (!trigger || !confirmModal) return;
        e.preventDefault();

        confirmModal.querySelector('#confirm-title').textContent = trigger.dataset.confirmTitle || '¿Confirmar acción?';
        confirmModal.querySelector('#confirm-text').textContent = trigger.dataset.confirm;
        confirmModal.querySelector('#confirm-ok').textContent = trigger.dataset.confirmOk || 'Confirmar';

        accionPendiente = function () {
            if (trigger.tagName === 'A') {
                window.location.href = trigger.href;
            } else if (trigger.form) {
                trigger.form.submit();
            }
        };
        abrirModal(confirmModal);
    });

    if (confirmModal) {
        confirmModal.querySelector('#confirm-ok').addEventListener('click', function () {
            if (accionPendiente) accionPendiente();
        });
    }

    // ---------- Alertas descartables ----------
    document.addEventListener('click', function (e) {
        const btn = e.target.closest('[data-dismiss]');
        if (btn) btn.closest('.alert').remove();
    });

    // Quitar ?exito=... de la URL para que al recargar no se repita el aviso
    if (/[?&](exito|error|cancelado|eliminado|correo)=?/.test(window.location.search) && window.history.replaceState) {
        window.history.replaceState(null, '', window.location.pathname);
    }

    // ---------- Filtro de tablas ----------
    // Uso: <input data-filter="idTabla">  +  opcional <span data-count-for="idTabla">
    // En listas que no son tablas, cada elemento filtrable lleva data-row.
    function filtrarTabla(tableId) {
        const tabla = document.getElementById(tableId);
        if (!tabla) return;
        const input = document.querySelector('[data-filter="' + tableId + '"]');
        const texto = normalizar(input ? input.value : '');
        const grupo = tabla.dataset.group || 'all';
        let visibles = 0;

        tabla.querySelectorAll('tbody tr:not(.no-results), [data-row]').forEach(function (fila) {
            const coincideTexto = !texto || normalizar(fila.textContent).includes(texto);
            const coincideGrupo = grupo === 'all' || (fila.dataset.group || '').split(' ').includes(grupo);
            const mostrar = coincideTexto && coincideGrupo;
            fila.style.display = mostrar ? '' : 'none';
            if (mostrar) visibles++;
        });

        const vacio = tabla.querySelector('.no-results');
        if (vacio) vacio.style.display = visibles ? 'none' : '';
        const contador = document.querySelector('[data-count-for="' + tableId + '"]');
        if (contador) contador.textContent = visibles + (visibles === 1 ? ' resultado' : ' resultados');
    }

    function normalizar(s) {
        return s.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
    }

    document.querySelectorAll('[data-filter]').forEach(function (input) {
        input.addEventListener('input', function () { filtrarTabla(input.dataset.filter); });
        filtrarTabla(input.dataset.filter);
    });

    // Segmentos de filtro: <div class="seg" data-seg-for="idTabla"><button data-value="all">
    document.querySelectorAll('[data-seg-for]').forEach(function (seg) {
        seg.addEventListener('click', function (e) {
            const btn = e.target.closest('button');
            if (!btn) return;
            seg.querySelectorAll('button').forEach(function (b) { b.classList.toggle('active', b === btn); });
            const tabla = document.getElementById(seg.dataset.segFor);
            tabla.dataset.group = btn.dataset.value;
            filtrarTabla(seg.dataset.segFor);
        });
    });

    window.SaludSync.filtrarTabla = filtrarTabla;
})();
