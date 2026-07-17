package com.careeros.api.gmail;

public interface GmailMessageClassifier {

	GmailMessageClassification classify(GmailMessageMetadata message);
}
