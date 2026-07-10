package com.csj.archive.market.product;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> list() {
        return productRepository.findAll();
    }

    @Transactional
    public List<ProductEntity> seed() {
        if (productRepository.count() > 0) {
            return productRepository.findAll();
        }
        productRepository.save(new ProductEntity("PROD-BATTERY-MODULE", ProductType.BATTERY_MODULE,
                "Synthetic Battery Module", bd("1200000"), bd("760000"), bd("0.3667"), true));
        productRepository.save(new ProductEntity("PROD-VEHICLE-PART", ProductType.VEHICLE_PART,
                "Synthetic Vehicle Part", bd("240000"), bd("150000"), bd("0.3750"), true));
        productRepository.save(new ProductEntity("PROD-ELECTRONIC-COMPONENT", ProductType.ELECTRONIC_COMPONENT,
                "Synthetic Electronic Component", bd("95000"), bd("59000"), bd("0.3789"), true));
        productRepository.save(new ProductEntity("PROD-SERVICE-CONTRACT", ProductType.SERVICE_CONTRACT,
                "Synthetic Service Contract", bd("420000"), bd("90000"), bd("0.7857"), true));
        productRepository.save(new ProductEntity("PROD-PREMIUM-SUPPORT", ProductType.PREMIUM_SUPPORT,
                "Synthetic Premium Support", bd("180000"), bd("45000"), bd("0.7500"), true));
        return productRepository.findAll();
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }
}
