package org.oso.core.controllers

import org.oso.core.dtos.*
import org.oso.core.entities.HelpProvider
import org.oso.core.exceptions.HelpProviderNotFoundException
import org.oso.core.services.HelpProviderService
import org.oso.core.services.SecurityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.net.URI

@Controller
@RequestMapping(HelpProviderController.PATH_HELP_PROVIDERS)
class HelpProviderController
    @Autowired
    constructor(private val helpProviderService: HelpProviderService,
                private val securityService: SecurityService) {

    @GetMapping
    @ResponseBody
    fun findAll(): List<HelpProviderDto> {
        return helpProviderService.findAll().map { it.toDto() }
    }

    @GetMapping("{id}")
    @ResponseBody
    fun findById(@PathVariable id: String): HelpProviderDto {
        return getHelpProviderOrFail(id).toDto()
    }

    @GetMapping("{id}/$PATH_HELP_REQUESTERS")
    @ResponseBody
    fun findHelpRequesters(@PathVariable id: String): List<HelpRequesterDto> {
        val helpProvider = getHelpProviderOrFail(id)
        return helpProviderService.findHelpRequesters(helpProvider.id!!).map { it.toDto() }
    }

    @PostMapping
    fun createHelpProvider(helpProvider: HelpProviderPushDto): ResponseEntity<Unit> {
        return helpProviderService.createHelpProvider(helpProvider.toEntity()).let {
            ResponseEntity.created(URI("$PATH_HELP_PROVIDERS/${it.id}")).build()
        }
    }

    // TODO eventually move to emergency controller
    @PostMapping(PATH_ACCEPT_EMERGENCY)
    fun acceptEmergency(@RequestBody emergencyAccepted: EmergencyAcceptedDto): ResponseEntity<Unit> {
        helpProviderService.acceptEmergency(
            emergencyId = emergencyAccepted.emergencyId,
            helpProviderId = emergencyAccepted.helpProviderId
        )

        return ResponseEntity.accepted().build<Unit>()
    }

    private fun getHelpProviderOrFail(id: String): HelpProvider {
        return helpProviderService.findById(id) ?: throw HelpProviderNotFoundException("HelpProvider<$id> not found")
    }

    private fun HelpProviderPushDto.toEntity() = HelpProvider(
        name = name,
        keycloakName = securityService.getCurrentUserName()
    )

    companion object {
        const val PATH_HELP_PROVIDERS = "help-providers"
        const val PATH_HELP_REQUESTERS = "help-requesters"
        const val PATH_ACCEPT_EMERGENCY = "accept-emergency"
    }
}