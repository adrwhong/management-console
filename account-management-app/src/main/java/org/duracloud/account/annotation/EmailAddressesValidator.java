/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.account.annotation;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.lang.StringUtils;
import org.duracloud.account.util.EmailAddressesParser;
import org.hibernate.validator.constraints.impl.EmailValidator;

/**
 * @author "Daniel Bernstein (dbernstein@duraspace.org)"
 * 
 */

public class EmailAddressesValidator
    implements ConstraintValidator<EmailAddressesConstraint, String> {
    private EmailAddressesConstraint constraintAnnotation;
    private static EmailValidator EMAIL_VALIDATOR = new EmailValidator();

    /*
     * (non-Javadoc)
     * 
     * @see javax.validation.ConstraintValidator#isValid(java.lang.Object,
     * javax.validation.ConstraintValidatorContext)
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> emailAddresses = EmailAddressesParser.parse(value);
        List<String> badAddresses = null;
        for (String ea : emailAddresses) {
            if (!EMAIL_VALIDATOR.isValid(ea, null)) {
                if (badAddresses == null) {
                    badAddresses = new LinkedList<String>();
                }

                badAddresses.add(ea);
            }
        }
        
        if(badAddresses != null){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(MessageFormat.format(constraintAnnotation.message(),
                StringUtils.join(badAddresses, ", ")))
                .addConstraintViolation();
        }
        return (badAddresses == null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.validation.ConstraintValidator#initialize(java.lang.annotation.
     * Annotation)
     */
    @Override
    public void initialize(EmailAddressesConstraint constraintAnnotation) {
        this.constraintAnnotation = constraintAnnotation;
    }

}
