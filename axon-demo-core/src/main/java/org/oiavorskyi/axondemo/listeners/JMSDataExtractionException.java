package org.oiavorskyi.axondemo.listeners;

import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.jms.JMSException;
import javax.jms.TextMessage;

public class JMSDataExtractionException extends Exception {

    private TextMessage originalJMSMessage;
    private Object      extractedObject;
    private Errors      validationErrors;

    private JMSDataExtractionException() {
    }

    private JMSDataExtractionException( String message ) {
        super(message);
    }

    private JMSDataExtractionException( String message, Throwable cause ) {
        super(message, cause);
    }

    private JMSDataExtractionException( Throwable cause ) {
        super(cause);
    }

    private JMSDataExtractionException( String message, Throwable cause, boolean enableSuppression,
                                        boolean writableStackTrace ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public JMSDataExtractionException( String message, Throwable cause,
                                       TextMessage originalJMSMessage,
                                       Object extractedObject, Errors validationErrors ) {
        super(message, cause);

        if ( originalJMSMessage == null ) {
            throw new IllegalArgumentException("originalJMSMessage has to be provided");
        }

        this.originalJMSMessage = originalJMSMessage;
        this.extractedObject = extractedObject;
        this.validationErrors = validationErrors;
    }

    public TextMessage getOriginalJMSMessage() {
        return originalJMSMessage;
    }

    public Object getExtractedObject() {
        return extractedObject;
    }

    public Errors getValidationErrors() {
        return validationErrors;
    }

    public String getDetailedDescription( boolean withMessagePayload, boolean
            replaceNewLineWithSpace ) {
        StringBuilder builder = new StringBuilder("{");
        if ( extractedObject == null ) {
            // Unmarshalling issue, no validation was performed
            builder.append("\n[issueType: UNEXPECTED_PAYLOAD]");
        } else if ( validationErrors != null ) {
            // Validation errors
            builder.append("\n[issueType: INVALID_REQUEST]");

            builder.append("\n[extractedObject: ");
            builder.append(extractedObject);
            builder.append("]");

            if ( validationErrors.getErrorCount() > 0 ) {
                builder.append("\n[errors: ");
                for ( FieldError error : validationErrors.getFieldErrors() ) {
                    builder.append(error.getField());
                    builder.append(" ");
                    builder.append(error.getDefaultMessage());
                    builder.append("; ");
                }
                for ( ObjectError error : validationErrors.getGlobalErrors() ) {
                    builder.append(error.getDefaultMessage());
                    builder.append("; ");
                }
                builder.append("]");
            }
        } else {
            builder.append("\n[issueType: GENERIC]");
        }

        if ( getCause() != null ) {
            Throwable rootCause = getCause();
            while ( rootCause.getCause() != null ) {
                rootCause = rootCause.getCause();
            }
            builder.append("\n[rootCause: ");
            builder.append(rootCause.getMessage());
            builder.append("]");
        }


        if ( withMessagePayload ) {
            try {
                builder.append("\n[messagePayload:\n");
                builder.append(originalJMSMessage.getText());
                builder.append("\n]");
            } catch ( JMSException e ) {
                builder.append("[payload: unable to extract from message]");
            }
        }

        builder.append("}");

        String description = builder.toString();
        if ( replaceNewLineWithSpace ) {
            description = description.replace('\n', ' ');
        }

        return description;
    }

    public static final class Builder {

        private final String      message;
        private       Throwable   cause;
        private final TextMessage jmsMessage;
        private       Object      extractedObject;
        private       Errors      validationErrors;

        private Builder( String message, TextMessage jmsMessage ) {
            if ( message == null || jmsMessage == null ) {
                throw new IllegalArgumentException("Mandatory parameter is null");
            }

            this.message = message;
            this.jmsMessage = jmsMessage;
        }

        public static Builder forJmsMessage( String message, TextMessage jmsMessage ) {
            return new Builder(message, jmsMessage);
        }

        public Builder withCause( Throwable cause ) {
            this.cause = cause;
            return this;
        }

        public Builder withExtractedObject( Object extractedObject ) {
            this.extractedObject = extractedObject;
            return this;
        }

        public Builder withValidationErrors( Errors validationErrors ) {
            this.validationErrors = validationErrors;
            return this;
        }

        public JMSDataExtractionException build() {
            return new JMSDataExtractionException(message, cause, jmsMessage, extractedObject,
                    validationErrors);
        }

    }

}
