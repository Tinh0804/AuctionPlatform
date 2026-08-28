package com.ecommerce.auctionplatform.identity.infrastructure.external;

import com.ecommerce.auctionplatform.shared.application.exception.AppException;
import com.ecommerce.auctionplatform.shared.application.exception.ErrorCode;
import com.ecommerce.auctionplatform.shared.application.model.FileContent;
import com.ecommerce.auctionplatform.identity.application.port.out.KycIdentity;
import com.ecommerce.auctionplatform.identity.application.port.out.KycVerificationPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class FptKycAdapter implements KycVerificationPort {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ObjectMapper objectMapper;

    @Value("${app.fptai.key}")
    private String apiKey;

    @Override
    public KycIdentity verify(FileContent frontImage) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("api-key", apiKey);

            ByteArrayResource image = new ByteArrayResource(frontImage.bytes()) {
                @Override
                public String getFilename() {
                    return frontImage.filename();
                }
            };
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", image);

            String response = new RestTemplate().postForObject(
                    "https://api.fpt.ai/vision/idr/vnm",
                    new HttpEntity<>(body, headers),
                    String.class);
            return parseIdentity(response);
        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AppException(ErrorCode.INVALID_EKYC_IMAGE);
        }
    }

    private KycIdentity parseIdentity(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        if (root.path("errorCode").asInt(-1) != 0 || !root.path("data").isArray()
                || root.path("data").isEmpty()) {
            throw new AppException(ErrorCode.INVALID_EKYC_IMAGE);
        }

        JsonNode data = root.path("data").get(0);
        String idCard = data.path("id").asText("");
        String dateOfBirth = data.path("dob").asText("");
        String gender = data.path("sex").asText("");
        LocalDate parsedDate = dateOfBirth.isBlank()
                ? null
                : LocalDate.parse(dateOfBirth.replace('-', '/'), DATE_FORMAT);
        boolean male = "Nam".equalsIgnoreCase(gender)
                || "Male".equalsIgnoreCase(gender)
                || "true".equalsIgnoreCase(gender);
        return new KycIdentity(idCard, parsedDate, male);
    }
}
