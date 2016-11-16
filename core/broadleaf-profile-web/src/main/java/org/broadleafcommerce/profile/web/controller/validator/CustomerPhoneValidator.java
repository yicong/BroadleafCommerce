/*
 * #%L
 * BroadleafCommerce Profile Web
 * %%
 * Copyright (C) 2009 - 2016 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package org.broadleafcommerce.profile.web.controller.validator;

import org.broadleafcommerce.profile.core.domain.CustomerPhone;
import org.broadleafcommerce.profile.core.domain.Phone;
import org.broadleafcommerce.profile.core.service.CustomerPhoneService;
import org.broadleafcommerce.profile.core.service.PhoneService;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.annotation.Resource;
import java.util.List;

@Component("blCustomerPhoneValidator")
public class CustomerPhoneValidator implements Validator {

    @Resource(name="blPhoneService")
    private final PhoneService phoneService;

    @Resource(name="blCustomerPhoneService")
    private final CustomerPhoneService customerPhoneService;

    public CustomerPhoneValidator(){
        this.phoneService = null;
        this.customerPhoneService = null;
    }

    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return clazz.equals(Phone.class);
    }

    public void validate(Object obj, Errors errors) {
        //use regular phone
        CustomerPhone cPhone = (CustomerPhone) obj;

        if (!errors.hasErrors()) {
            //check for duplicate phone number
            List<CustomerPhone> customerPhones = customerPhoneService.readAllCustomerPhonesByCustomerId(cPhone.getCustomer().getId());

            Phone phone = phoneService.readPhoneById(cPhone.getPhoneExternalId());
            String phoneNum = phone.getPhoneNumber();
            String phoneName = cPhone.getPhoneName();

            Long phoneId = phone.getId();
            Long customerPhoneId = cPhone.getId();

            boolean foundPhoneIdForUpdate = false;
            boolean foundCustomerPhoneIdForUpdate = false;

            for (CustomerPhone existingCustomerPhone : customerPhones) {
                Phone existingPhone = phoneService.readPhoneById(cPhone.getPhoneExternalId());

                //validate that the phoneId passed for an editPhone scenario exists for this user
                if(phoneId != null && !foundPhoneIdForUpdate){
                    if(existingPhone.getId().equals(phoneId)){
                        foundPhoneIdForUpdate = true;
                    }
                }

                //validate that the customerPhoneId passed for an editPhone scenario exists for this user
                if(customerPhoneId != null && !foundCustomerPhoneIdForUpdate){
                    if(existingCustomerPhone.getId().equals(customerPhoneId)){
                        foundCustomerPhoneIdForUpdate = true;
                    }
                }

                if(existingCustomerPhone.getId().equals(cPhone.getId())){
                    continue;
                }

                if(phoneNum.equals(existingPhone.getPhoneNumber())){
                    errors.pushNestedPath("phone");
                    errors.rejectValue("phoneNumber", "phoneNumber.duplicate", null);
                    errors.popNestedPath();
                }

                if(phoneName.equalsIgnoreCase(existingCustomerPhone.getPhoneName())){
                    errors.rejectValue("phoneName", "phoneName.duplicate", null);
                }
            }

            if(phoneId != null && !foundPhoneIdForUpdate){
                errors.pushNestedPath("phone");
                errors.rejectValue("id", "phone.invalid_id", null);
                errors.popNestedPath();
            }

            if(customerPhoneId != null && !foundCustomerPhoneIdForUpdate){
                errors.rejectValue("id", "phone.invalid_id", null);
            }
        }
    }
}
