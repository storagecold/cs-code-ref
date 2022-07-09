package com.wu.onep.adh.bizsvc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wu.onep.adh.bizsvc.config.BizSvcConfig;
import com.wu.onep.adh.bizsvc.mapper.LocationRequestMapper;
import com.wu.onep.adh.bizsvc.mapper.TerminalMapperAp;
import com.wu.onep.adh.bizsvc.model.*;
import com.wu.onep.adh.bizsvc.model.adh.LocationRootAdh;
import com.wu.onep.adh.bizsvc.model.ap.adh.*;
import com.wu.onep.adh.bizsvc.model.ap.sf.*;
import com.wu.onep.adh.bizsvc.model.cl.AdhClosingLogRequest;
import com.wu.onep.adh.bizsvc.model.cl.CLResponse;
import com.wu.onep.adh.bizsvc.model.sf.LocationRootSFC;
import com.wu.onep.adh.bizsvc.model.sf.SfResponse;
import com.wu.onep.adh.bizsvc.service.BizSvcServiceImpl;
import com.wu.onep.adh.bizsvc.service.BizSvcServiceImplAp;
import com.wu.onep.adh.bizsvc.util.Constants;
import com.wu.onep.adh.bizsvc.validation.*;
import com.wu.onep.library.exception.errorresponse.WUError;
import com.wu.onep.library.exception.exceptiontype.WUExceptionType;
import com.wu.onep.library.exception.exceptiontype.WUServiceException;
import com.wu.onep.library.exception.utils.WUServiceExceptionUtils;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Generated;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Objects;

import static com.wu.onep.adh.bizsvc.util.Constants.*;
import static com.wu.onep.adh.bizsvc.util.LogConstants.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/org/bizsvc")
@Tag(name = BIZSVC_NAME, description = BIZSVC_DESCRIPTION)
public class BizSvcController {
    private static final Logger logger = LogManager.getLogger(BizSvcController.class);
    private static final String X_WU_EXTERNALREFID = "x-wu-externalRefId";
    public static final String LOCATION_METHOD_IN_BIZ_SVC_CONTROLLER = "Enter into addLocation method in BizSvcController";
    public static final String LOCATION_METHOD_AP_IN_BIZ_SVC_CONTROLLER = "Enter into Agent Portal manager Location method in BizSvcController";
    //MTE location Adh end point
    public static final String LOCATION_MGMT = "/locationMgmt";
    //Agent-Portal location Adh end point
    public static final String LOCATION_MGMT_AP = "/apLocationMgmt";
    public static final String TERMINAL_MGMT_AP = "/apTerminalMgmt";
    public static final String VOID_CHECK = "/checVoid";
    public static final String SUPPLY_ORDER = "/supplyOrder";
    public static final String ENTER_PROCESS_CLOSING_LOG_METHOD = "Enter processClosingLog method";
    public static final String EXIT_PROCESS_CLOSING_LOG_METHOD = "Exit processClosingLog method";
    public static final String AP_OPERATOR_MGT = "/apOperatorMgmt";
    public static final String MANAGE_OPERATOR_METHOD_INPUT = "manageOperator method, input {}";
    public static final String OPERATOR_ROOT_SF_AP = "operatorRootSfAp {}";
    public static final String RESPONSE_OUT = "Response : {}";
    public static final String LIMIT_UPDATE = "/limitUpdate";
    public static final String LIMIT_UPDATE_INPUT = "LimitUpdate Input {}";
    public static final String LIMIT_OVERRIDE_SF = "Limit Override SF {}";
    public static final String SFC_URL = "SFC URL : {}";
    public static final String SUCCESS = "Success";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BizSvcServiceImpl bizSvcService;

    @Autowired
    private BizSvcServiceImplAp bizSvcServiceAp;

    @Autowired
    private WUServiceExceptionUtils exceptionUtils;

    @Autowired
    private BizSvcConfig bizSvcConfig;

    @Autowired
    private LocationValidation locationValidation;

    @Autowired
    private LocationValidationAp locationValidationAp;

    @Autowired
    private TerminalValidationAp terminalValidationAp;

    @Autowired
    private LocationRequestMapper locationRequestMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private OperatorValidationAp operatorValidationAp;

    @Autowired
    private LimitUpdateValidationAp limitUpdateValidationAp;

    @Autowired
    private VoidCheckValidationAp voidCheckValidationAp;

    @Autowired
    private TerminalMapperAp terminalMapperAp;

    @Autowired
    private SupplyOrderValidationAp supplyOrderValidationAp;

    @PostMapping(value = "/limitoverride", consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = "x-wu-externalRefId", description = "Echo back the request id sent by calling application.", schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = AdhLimitOverride.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = "x-wu-externalRefId", description = "Echo back the request id sent by calling application.", schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = "x-wu-externalRefId", description = "Echo back the request id sent by calling application.", schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    @Generated
    public ResponseEntity<AdhOverRideLimitResponse> getOverrideLimits(
            @RequestHeader(name = "x-api-key", required = true) String apikey,
            @RequestHeader(name = "x-wu-externalRefId", required = false) String extRefId,
            @RequestBody AdhLimitOverride requestModel) {
        try {
            logger.info(GET_OVERRIDELIMITS_ENTER);
            if (requestModel.getRequest().getAgentAccount().size() > OVERRIDE_LIMIT_COUNT) {
                throw exceptionUtils.prepareWuException(APIEX08, new RuntimeException(OVERRIDE_LIMIT_COUNT_ERROR),
                        WUExceptionType.BUSINESS);
            }
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            AdhOverRideLimitResponse response = new AdhOverRideLimitResponse();
            logger.info(GET_OVERRIDELIMITS_TOKEN, tokenSuccess);
            logger.info(" bizSvcConfig getOverrideLimitation {}", bizSvcConfig.getOverrideLimitation());
            if (StringUtils.isNotBlank(tokenSuccess)) {
                response = requestModel.getRequest().getAgentAccount().size() <= bizSvcConfig.getOverrideLimitation() ?
                        bizSvcService.overideLimit(requestModel, tokenSuccess) :
                        bizSvcService.bulkOverrideLimit(requestModel, tokenSuccess);
            }

            if (response.getTicketStatus() == null && response.getResponseBody() != null) {
                if (response.getResponseBody().contains(DUPLICATE_VALUE)) {
                    throw exceptionUtils.prepareWuException(APIEX07, new RuntimeException(UPDATE_DDL_ERROR_VALUE),
                            WUExceptionType.BUSINESS);
                }
                if (response.getResponseCode().equals(BAD_REQUEST)) {
                    throw exceptionUtils.prepareWuException(APIEX02, new RuntimeException(UPDATE_DDL_ERROR_VALUE),
                            WUExceptionType.BUSINESS);
                }
            }
            if (response.getStatus().equalsIgnoreCase(RESPONSE_FAIL_VALUE)) {
                throw exceptionUtils.prepareWuException(APIEX06, new RuntimeException(UPDATE_DDL_ERROR_VALUE),
                        WUExceptionType.BUSINESS);
            }
            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.set(X_WU_EXTERNALREFID, extRefId);
            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (NumberFormatException ex) {
            throw exceptionUtils.prepareWuException(APIEX09, new RuntimeException(NUMBER_FORMAT_EXCEPTION),
                    WUExceptionType.BUSINESS);
        } catch (Exception ex) {
            logger.error(ex);
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }

    @PostMapping(value = "/ticketStatus", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = AdhTicketStatus.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    @Generated
    public ResponseEntity<AdhTicketStatusResponse> getTicketStatus(
            @RequestHeader(name = X_API_KEY, required = true) String apikey,
            @RequestHeader(name = X_WU_EXTERNALREFID, required = false) String extRefId,
            @RequestBody AdhTicketStatus requestModel) throws JsonProcessingException {
        try {
            logger.info(GET_TICKET_STATUS_ENTER);
            String payLoadSanitize = HtmlUtils.htmlEscape(requestModel.toString(), "UTF-8");
            String payLoad = HtmlUtils.htmlUnescape(payLoadSanitize);
            AdhTicketStatus ticketStatus = objectMapper.readValue(payLoad, AdhTicketStatus.class);
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.set(X_WU_EXTERNALREFID, extRefId);
            TicketStatusResponse respMsg = null;
            String agentAccounId = null;
            logger.info(GET_TICKET_STATUS_TOKEN, tokenSuccess);
            if (StringUtils.isNotBlank(tokenSuccess)) {
                respMsg = bizSvcService.getTicketStatus(ticketStatus, tokenSuccess);
            }
            AdhTicketStatusResponse response = new AdhTicketStatusResponse();
            if (Objects.nonNull(respMsg)) {
                if (!respMsg.getTickets().isEmpty()
                        && Constants.CHANNEL_ID_MTE.equalsIgnoreCase(requestModel.getRequest().getChannelId())
                        && respMsg.getTickets().get(0).getTicketStatus().equals(SUCCESS)) {

                    TicketStatusResponse detailsResponse = bizSvcService.getTicketStatusWithDetails(requestModel, tokenSuccess);
                    if (detailsResponse != null) {
                        agentAccounId = detailsResponse.getTickets().get(0).getAgentAccount().get(0).getAgentAccountID();
                    }

                }

                if (!respMsg.getTickets().isEmpty()) {
                    setTicketStatus(requestModel, respMsg, agentAccounId, response);
                }
                if (CollectionUtils.isEmpty(respMsg.getTickets())) {
                    throw exceptionUtils.prepareWuException(APIEX01, new RuntimeException(NO_RECORDS_FOUND_VALUE),
                            WUExceptionType.BUSINESS);
                }
            }

            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (WUServiceException ex) {
            logger.error(ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex);
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }

    private void setTicketStatus(@RequestBody AdhTicketStatus requestModel, TicketStatusResponse respMsg, String agentAccounId, AdhTicketStatusResponse response) {
        response.setTicketNumber(respMsg.getTickets().get(0).getTicketNumber());
        response.setResponseCode(respMsg.getResponseCode());
        response.setErrorCode(respMsg.getTickets().get(0).getErrorCode());
        response.setErrorMessage(respMsg.getTickets().get(0).getErrorMessage());
        response.setResponseCode(respMsg.getResponseCode());
        response.setTicketStatus(respMsg.getTickets().get(0).getTicketStatus());
        getTicketStatus(respMsg, response);
        if (Constants.CHANNEL_ID_MTE.equalsIgnoreCase(requestModel.getRequest().getChannelId()) && agentAccounId != null) {
            AgentAccountResponse agentAccount = new AgentAccountResponse();
            agentAccount.setAgentAccountId(agentAccounId);
            response.setAgentAccounts(new ArrayList<>());
            response.getAgentAccounts().add(agentAccount);
        }
    }

    @Generated
    private void getTicketStatus(TicketStatusResponse respMsg, AdhTicketStatusResponse response) {
        if (Objects.nonNull(respMsg.getTickets().get(0).getAgentAccount())) {
            response.setAgentAccounts(new ArrayList<>());
            for (AgentAccountStaging agentAccountStaging : respMsg.getTickets().get(0).getAgentAccount()) {
                AgentAccountResponse agentAccount = new AgentAccountResponse();
                agentAccount.setAgentAccountId(agentAccountStaging.getAgentAccountID());
                Boolean updateSuccessful = agentAccountStaging.getErrorDescription() != null ? Boolean.FALSE : Boolean.TRUE;
                agentAccount.setSuccess(updateSuccessful.toString());
                agentAccount.setDailyLimit(agentAccountStaging.getDailyLimit());
                String agentAccountId = StringUtils.isNotEmpty(agentAccount.getAgentAccountId()) ? agentAccount.getAgentAccountId() : STRING_EMPTY;
                String ticketNumber = StringUtils.isNotEmpty(response.getTicketNumber()) ? response.getTicketNumber() : STRING_EMPTY;
                String statusLimit = "";
                statusLimit = getStatusLimit(updateSuccessful, agentAccountId, ticketNumber);
                agentAccount.setStatus(statusLimit);
                agentAccount.setCountry(agentAccountStaging.getCountry());
                agentAccount.setAgentAccountCurrency(agentAccountStaging.getAgentAccountCurrency());
                agentAccount.setErrorDescription(agentAccountStaging.getErrorDescription());
                response.getAgentAccounts().add(agentAccount);
            }
        }
    }

    @Generated
    private String getStatusLimit(Boolean updateSuccessful, String agentAccountId, String ticketNumber) {
        String statusLimit;
        if (updateSuccessful) {
            statusLimit = GET_TICKET_STATUS_MESSAGE1.concat(agentAccountId)
                    .concat(GET_TICKET_STATUS_MESSAGE2)
                    .concat(ticketNumber)
                    .concat(GET_TICKET_STATUS_MESSAGE3);
        } else {
            statusLimit = GET_TICKET_STATUS_MESSAGE4.concat(agentAccountId)
                    .concat(GET_TICKET_STATUS_MESSAGE2)
                    .concat(ticketNumber);
        }
        return statusLimit;
    }

    @PostMapping(path = LOCATION_MGMT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = LocationRootAdh.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    @Generated
    public ResponseEntity<LocationResponse> manageLocation(
            @RequestHeader(name = X_API_KEY, required = true) String apikey,
            @RequestHeader(name = X_WU_EXTERNALREFID, required = false) String extRefId,
            @RequestBody LocationRootAdh locationRootADH) throws JsonProcessingException {
        try {
            logger.info(LOCATION_METHOD_IN_BIZ_SVC_CONTROLLER);
            //validate MTE Input.
            locationValidation.validateMteLocation(locationRootADH);
            //Map Request from ADH to SFC.
            LocationRootSFC locationRootSFC = locationRequestMapper.mappingMteLocationReq(locationRootADH);
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            HttpHeaders respHeaders = new HttpHeaders();
            LocationResponse response = new LocationResponse();
            logger.info(GET_TICKET_STATUS_TOKEN, tokenSuccess);
            if (tokenSuccess != null) {
                respHeaders.set(X_WU_EXTERNALREFID, extRefId);
                response = bizSvcService.manageLocation(locationRootSFC, tokenSuccess);
            }
            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (WUServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }

    @PostMapping(value = "/closinglog", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = AdhClosingLogRequest.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    @Generated
    public ResponseEntity<CLResponse> processClosingLog(
            @RequestHeader(name = X_API_KEY, required = true) String apikey,
            @RequestHeader(name = X_WU_EXTERNALREFID, required = false) String extRefId,
            @RequestBody AdhClosingLogRequest requestModel) {
        try {
            logger.info(ENTER_PROCESS_CLOSING_LOG_METHOD);
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            HttpHeaders respHeaders = new HttpHeaders();
            respHeaders.set(X_WU_EXTERNALREFID, extRefId);
            logger.info(GET_TICKET_STATUS_TOKEN, tokenSuccess);
            CLResponse response = null;
            if (tokenSuccess != null) {
                respHeaders.set(X_WU_EXTERNALREFID, extRefId);
                response = bizSvcService.processClosingLog(requestModel, tokenSuccess);
            }
            logger.info(EXIT_PROCESS_CLOSING_LOG_METHOD);
            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (Exception ex) {
            logger.error(ex);
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }

    @PostMapping(path = LOCATION_MGMT_AP, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = LocationRootAdhAp.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    @Generated
    public ResponseEntity<SfResponse> manageLocationAp(
            @RequestHeader(name = X_API_KEY, required = true) String apikey,
            @RequestHeader(name = X_WU_EXTERNALREFID, required = false) String extRefId,
            @RequestBody LocationRootAdhAp locationRootAdhAp) {
        try {
            logger.info(LOCATION_METHOD_AP_IN_BIZ_SVC_CONTROLLER);
            //validate Agent Portal Input.
            locationValidationAp.validateLocationAp(locationRootAdhAp);
            //Map Request from ADH to SFC.
            LocationRootSfAp locationRootSfAp = modelMapper.map(locationRootAdhAp, LocationRootSfAp.class);
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            HttpHeaders respHeaders = new HttpHeaders();
            SfResponse response = new SfResponse();
            logger.info(GET_TICKET_STATUS_TOKEN, tokenSuccess);
            if (tokenSuccess != null) {
                respHeaders.set(X_WU_EXTERNALREFID, extRefId);
                response = bizSvcServiceAp.manageLocationAp(locationRootSfAp, tokenSuccess);
            }
            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (WUServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }

    @PostMapping(path = TERMINAL_MGMT_AP, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = TerminalRootAdhAp.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    @Generated
    public ResponseEntity<SfResponse> manageTerminalAp(
            @RequestHeader(name = X_API_KEY, required = true) String apikey,
            @RequestHeader(name = X_WU_EXTERNALREFID, required = false) String extRefId,
            @RequestBody TerminalRootAdhAp terminalRootAdhAp) {
        try {
            logger.info("Enter into Agent Portal manager terminal method in BizSvcController");
            //validate Agent Portal Input.
            terminalValidationAp.validateTerminalAp(terminalRootAdhAp);
            //Map Request from ADH to SFC.
            TerminalRootSfAp terminalRootSfAp = terminalMapperAp.mapTerminalAp(terminalRootAdhAp);
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            HttpHeaders respHeaders = new HttpHeaders();
            SfResponse response = new SfResponse();
            logger.info(GET_TICKET_STATUS_TOKEN, tokenSuccess);
            if (tokenSuccess != null) {
                respHeaders.set(X_WU_EXTERNALREFID, extRefId);
                response = bizSvcServiceAp.manageTerminalAp(terminalRootSfAp, tokenSuccess);
            }
            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (WUServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }

    @PostMapping(path = AP_OPERATOR_MGT, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = SfResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    public ResponseEntity<SfResponse> manageOperator(
            @RequestHeader(name = X_API_KEY, required = true) String apikey,
            @RequestHeader(name = X_WU_EXTERNALREFID, required = false) String extRefId,
            @RequestBody OperatorRootAdhAp operatorRootAdhAp) {
        try {
            logger.info(MANAGE_OPERATOR_METHOD_INPUT, operatorRootAdhAp);
            operatorValidationAp.validateOperatorAp(operatorRootAdhAp);
            //Map Request from ADH to SFC.
            OperatorRootSfAp operatorRootSfAp = modelMapper.map(operatorRootAdhAp, OperatorRootSfAp.class);
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            HttpHeaders respHeaders = new HttpHeaders();
            SfResponse response = new SfResponse();
            logger.info(GET_TICKET_STATUS_TOKEN, tokenSuccess);
            if (tokenSuccess != null) {
                respHeaders.set(X_WU_EXTERNALREFID, extRefId);
                logger.info(OPERATOR_ROOT_SF_AP, operatorRootAdhAp);
                logger.info(SFC_URL, bizSvcConfig.getSalesforceOperator());
                response = bizSvcServiceAp.invokeSalesforceApi(operatorRootSfAp, bizSvcConfig.getSalesforceOperator(), tokenSuccess);
                logger.info(RESPONSE_OUT, response);
            }
            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (WUServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }

    @PostMapping(path = LIMIT_UPDATE, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = SfResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    @Generated
    public ResponseEntity<SfResponse> limitUpdateAp(
            @RequestHeader(name = X_API_KEY, required = true) String apikey,
            @RequestHeader(name = X_WU_EXTERNALREFID, required = false) String extRefId,
            @RequestBody LimitUpdateRootAdhAp limitUpdateRootAdhAp) {
        try {
            logger.info(LIMIT_UPDATE_INPUT, limitUpdateRootAdhAp);
            limitUpdateValidationAp.validateLimitUpdateAp(limitUpdateRootAdhAp);
            //Map Request from ADH to SFC.
            OverrideLimitRootSfAp limitOverride = new OverrideLimitRootSfAp(limitUpdateRootAdhAp);
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            HttpHeaders respHeaders = new HttpHeaders();
            SfResponse response = new SfResponse();
            logger.info(GET_TICKET_STATUS_TOKEN, tokenSuccess);
            if (tokenSuccess != null) {
                respHeaders.set(X_WU_EXTERNALREFID, extRefId);
                logger.info(LIMIT_OVERRIDE_SF, limitOverride);
                response = bizSvcServiceAp.invokeSalesforceApi(limitOverride, bizSvcConfig.getSalesforceOverrideLimit(), tokenSuccess);
                logger.info(RESPONSE_OUT, response);
            }
            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (WUServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }


    @PostMapping(path = VOID_CHECK, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = VoidCheckRootAdhAp.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    public ResponseEntity<SfResponse> manageVoidCheckAp(
            @RequestHeader(name = X_API_KEY, required = true) String apikey,
            @RequestHeader(name = X_WU_EXTERNALREFID, required = false) String extRefId,
            @RequestBody VoidCheckRootAdhAp voidCheckRootAdhAp) {
        try {
            logger.info("Enter into Agent Portal manager void check method in BizSvcController");
            //validate Agent Portal Input.
            voidCheckValidationAp.validateVoidCheckAp(voidCheckRootAdhAp);
            //Map Request from ADH to SFC.
            VoidCheckRootSfAp voidCheckRootSfAp = modelMapper.map(voidCheckRootAdhAp, VoidCheckRootSfAp.class);
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            HttpHeaders respHeaders = new HttpHeaders();
            SfResponse response = new SfResponse();
            logger.info(GET_TICKET_STATUS_TOKEN, tokenSuccess);
            if (tokenSuccess != null) {
                respHeaders.set(X_WU_EXTERNALREFID, extRefId);
                response = bizSvcServiceAp.manageVoidCheckAp(voidCheckRootSfAp, tokenSuccess);
            }
            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (WUServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }


    @PostMapping(path = SUPPLY_ORDER, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(array = @ArraySchema(schema = @Schema(implementation = VoidCheckRootAdhAp.class)))),
            @ApiResponse(responseCode = "400", description = "Bad Request", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", headers = @Header(name = X_WU_EXTERNALREFID, description = X_WU_EXTERNALREFID_DESCRIPTION, schema = @Schema(type = "string")), content = @Content(schema = @Schema(implementation = WUError.class)))})
    public ResponseEntity<SfResponse> manageSupplyOrderAp(
            @RequestHeader(name = X_API_KEY, required = true) String apikey,
            @RequestHeader(name = X_WU_EXTERNALREFID, required = false) String extRefId,
            @RequestBody SupplyOrderRootAdhAp supplyOrderRootAdhAp) {
        try {
            logger.info("Enter into Agent Portal manager supply order method in BizSvcController");
            //validate Agent Portal Input.
            supplyOrderValidationAp.validateSupplyOrderAp(supplyOrderRootAdhAp.getRequest());
            //Map Request from ADH to SFC.
            SupplyOrderRootSfAp supplyOrderRootSfAp = modelMapper.map(supplyOrderRootAdhAp, SupplyOrderRootSfAp.class);
            String tokenSuccess = bizSvcService.connectionToSalesforce();
            HttpHeaders respHeaders = new HttpHeaders();
            SfResponse response = new SfResponse();
            logger.info(GET_TICKET_STATUS_TOKEN, tokenSuccess);
            if (tokenSuccess != null) {
                respHeaders.set(X_WU_EXTERNALREFID, extRefId);
                response = bizSvcServiceAp.manageSupplyOrderAp(supplyOrderRootSfAp, tokenSuccess);
            }
            return new ResponseEntity<>(response, respHeaders, HttpStatus.OK);
        } catch (WUServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw exceptionUtils.prepareWuException(APIEX03, new RuntimeException(INTERNAL_SERVER_ERROR),
                    WUExceptionType.SERVER);
        }
    }
}
