package com.nakacorp.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller para servir páginas públicas (HTML estático).
 * <p>
 * Este controller fornece redirecionamentos simples para páginas HTML
 * estáticas localizadas em resources/static
 * </p>
 *
 * @author Klleriston Andrade
 * @version 1.0
 * @since 2025-01-12
 */
@Controller
public class PublicPageController {

    /**
     * Redireciona /leads para a página de captação de leads.
     *
     * @return Redirect para a página HTML estática
     */
    @GetMapping("/leads")
    public String leadsPage() {
        return "redirect:/lead-capture.html";
    }

    /**
     * Página inicial - pode redirecionar para a captação de leads
     * ou para outra página que você preferir.
     *
     * @return Redirect para a página HTML estática
     */
    @GetMapping("/")
    public String homePage() {
        return "redirect:/lead-capture.html";
    }
}
