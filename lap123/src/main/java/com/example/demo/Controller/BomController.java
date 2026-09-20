package com.example.demo.Controller;

import com.example.demo.Service.BomService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bom")
public class BomController {
    private final BomService service;

    public BomController(BomService service) {
        this.service = service;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("boms", service.list());
        return "mrp/bom-list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("products", service.products());
        model.addAttribute("materials", service.materials());
        return "mrp/bom-form";
    }

    @PostMapping
    public String create(Authentication authentication, @RequestParam int productId, @RequestParam String version,
                         @RequestParam(required = false) String note,
                         @RequestParam List<Long> materialId,
                         @RequestParam List<BigDecimal> quantity,
                         @RequestParam List<BigDecimal> waste,
                         RedirectAttributes redirect) {
        try {
            long id = service.create(authentication.getName(), productId, version, note, materialId, quantity, waste);
            return "redirect:/bom/" + id;
        } catch (IllegalArgumentException | DataAccessException exception) {
            redirect.addFlashAttribute("error", exception instanceof DataAccessException ?
                    "Không lưu được BOM. Kiểm tra sản phẩm, nguyên vật liệu và phiên bản." : exception.getMessage());
            return "redirect:/bom/create";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("bom", service.get(id));
        model.addAttribute("details", service.details(id));
        return "mrp/bom-detail";
    }

    @PostMapping("/{id}/disable")
    public String disable(@PathVariable long id, RedirectAttributes redirect) {
        try {
            service.disable(id);
        } catch (IllegalArgumentException exception) {
            redirect.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/bom/" + id;
    }
}
