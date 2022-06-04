package org.ibctf.service;

import org.ibctf.model.Partner;
import org.ibctf.model.ShoppingItem;
import org.ibctf.repository.PartnerRepository;
import org.ibctf.repository.ShoppingItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TemplateService {

    private static final Pattern INVALID_TEMPLATE_PATTERN =
            Pattern.compile("runtime|exec|process|cmd|bash|start|command|entit", Pattern.CASE_INSENSITIVE);

    private final PartnerRepository partnerRepository;
    private final ShoppingItemRepository shoppingItemRepository;

    @Autowired
    public TemplateService(PartnerRepository partnerRepository, ShoppingItemRepository shoppingItemRepository) {
        this.partnerRepository = partnerRepository;
        this.shoppingItemRepository = shoppingItemRepository;
    }

    public void submitUserTemplate(Partner partner, String template) {
        Matcher matcher = INVALID_TEMPLATE_PATTERN.matcher(template);
        if (matcher.find()) {
            throw new InvalidParameterException("invalid template");
        }
        partner.setTemplate(template);
        partnerRepository.save(partner);
    }

    public void removeUserTemplate(Partner partner) {
        partner.setTemplate(null);
        partnerRepository.save(partner);
    }

    public String fetchUserTemplateOrDefault(Partner partner) throws IOException {
        String template = partner.getTemplate();
        if (template == null || template.isEmpty()) {
            ClassPathResource resource = new ClassPathResource("template.xslt");
            byte[] templateBytes = resource.getInputStream().readAllBytes();
            template = new String(templateBytes);
        }
        return template;
    }

    public String processItem(Long id, Partner partner) throws Exception {
        String template = fetchUserTemplateOrDefault(partner);

        Optional<ShoppingItem> item = shoppingItemRepository.findByIdAndPartner(id, partner);
        if (item.isEmpty()) {
            return null;
        }

        return render(template.getBytes(), item.get());
    }

    public String render(byte[] template, ShoppingItem item) throws Exception {
        StringWriter sw = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(ShoppingItem.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(item, sw);

        InputStream is = new ByteArrayInputStream(template);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Source source = new StreamSource(is);
        Transformer t = TransformerFactory.newInstance().newTransformer(source);
        t.transform(
                new StreamSource(new ByteArrayInputStream(sw.toString().getBytes())),
                new StreamResult(os)
        );

        return os.toString();
    }
}
