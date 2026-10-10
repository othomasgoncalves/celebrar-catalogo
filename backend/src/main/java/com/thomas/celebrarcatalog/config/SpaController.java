package com.thomas.celebrarcatalog.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Rotas do React Router devolvem o index.html, para que um refresh ou link direto
 * (ex.: /admin/produtos) abra a SPA em vez de um 404.
 */
@Controller
class SpaController {

    @GetMapping({"/montar", "/admin", "/admin/**"})
    String index() {
        return "forward:/index.html";
    }
}
