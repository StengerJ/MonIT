package com.monit.server.web;

import com.monit.server.entity.AlertRecipientEntity;
import com.monit.server.entity.ClientEntity;
import com.monit.server.entity.ClientStatus;
import com.monit.server.entity.MetricEntity;
import com.monit.server.repository.AlertRecipientRepository;
import com.monit.server.repository.CheckResultRepository;
import com.monit.server.repository.ClientRepository;
import com.monit.server.repository.MetricRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Controller
public class DashboardController {

    private final ClientRepository clientRepository;
    private final MetricRepository metricRepository;
    private final CheckResultRepository checkResultRepository;
    private final AlertRecipientRepository alertRecipientRepository;

    public DashboardController(ClientRepository clientRepository,
                                MetricRepository metricRepository,
                                CheckResultRepository checkResultRepository,
                                AlertRecipientRepository alertRecipientRepository) {
        this.clientRepository = clientRepository;
        this.metricRepository = metricRepository;
        this.checkResultRepository = checkResultRepository;
        this.alertRecipientRepository = alertRecipientRepository;
    }

    @GetMapping("/")
    public String overview(Model model) {
        List<ClientEntity> clients = clientRepository.findAll();
        model.addAttribute("clients", clients);
        model.addAttribute("onlineCount", clients.stream().filter(c -> c.getStatus() == ClientStatus.ONLINE).count());
        model.addAttribute("warningCount", clients.stream().filter(c -> c.getStatus() == ClientStatus.WARNING).count());
        model.addAttribute("offlineCount", clients.stream().filter(c -> c.getStatus() == ClientStatus.OFFLINE).count());
        return "index";
    }

    @GetMapping("/clients/{id}")
    public String clientDetail(@PathVariable("id") UUID id, Model model) {
        model.addAttribute("client", clientRepository.findById(id).orElseThrow());
        MetricEntity latest = metricRepository.findFirstByClientIdOrderByTimeDesc(id);
        model.addAttribute("latestMetric", latest);
        model.addAttribute("checkResults", checkResultRepository.findTop20ByClientIdOrderByTimeDesc(id));
        model.addAttribute("clientRecipients", alertRecipientRepository.findByClientId(id));
        return "client-detail";
    }

    @PostMapping("/clients/{id}/alerts")
    public String addClientRecipient(@PathVariable("id") UUID id, @RequestParam("email") String email) {
        AlertRecipientEntity recipient = new AlertRecipientEntity();
        recipient.setId(UUID.randomUUID());
        recipient.setEmail(email);
        recipient.setClientId(id);
        alertRecipientRepository.save(recipient);
        return "redirect:/clients/" + id;
    }

    @PostMapping("/clients/{id}/alerts/{recipientId}/delete")
    public String removeClientRecipient(@PathVariable("id") UUID id, @PathVariable("recipientId") UUID recipientId) {
        alertRecipientRepository.deleteById(recipientId);
        return "redirect:/clients/" + id;
    }

    @GetMapping("/settings/alerts")
    public String alertsSettings(Model model) {
        model.addAttribute("recipients", alertRecipientRepository.findByClientIdIsNull());
        model.addAttribute("newRecipient", new AlertRecipientEntity());
        return "alerts-settings";
    }

    @PostMapping("/settings/alerts")
    public String addRecipient(@RequestParam("email") String email) {
        AlertRecipientEntity recipient = new AlertRecipientEntity();
        recipient.setId(UUID.randomUUID());
        recipient.setEmail(email);
        alertRecipientRepository.save(recipient);
        return "redirect:/settings/alerts";
    }

    @PostMapping("/settings/alerts/{id}/delete")
    public String removeRecipient(@PathVariable("id") UUID id) {
        alertRecipientRepository.deleteById(id);
        return "redirect:/settings/alerts";
    }
}
