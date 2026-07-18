package com.careeros.api.gmail;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.careeros.api.application.persistence.JobApplicationEntity;

import org.springframework.stereotype.Component;

@Component
public class RuleBasedGmailApplicationMatcher implements GmailApplicationMatcher {

	@Override
	public Optional<GmailApplicationMatch> findSuggestion(
			GmailMessageMetadata message,
			List<JobApplicationEntity> applications) {
		String searchableText = normalize(message.sender() + " " + message.subject());
		List<ScoredApplication> matches = applications.stream()
				.map(application -> score(application, searchableText))
				.filter(scored -> scored.score() > 0)
				.sorted(Comparator.comparingInt(ScoredApplication::score).reversed())
				.toList();

		if (matches.isEmpty()) {
			return Optional.empty();
		}

		ScoredApplication bestMatch = matches.getFirst();
		if (matches.size() > 1 && matches.get(1).score() == bestMatch.score()) {
			return Optional.empty();
		}

		String reason = bestMatch.roleMatched()
				? "Company and role appear in the sender or subject"
				: "Company appears in the sender or subject";
		return Optional.of(new GmailApplicationMatch(
				bestMatch.application(),
				bestMatch.score(),
				reason));
	}

	private ScoredApplication score(
			JobApplicationEntity application,
			String searchableText) {
		String companyName = normalize(application.getCompany().getName());
		boolean companyMatched = containsPhrase(searchableText, companyName);
		if (!companyMatched) {
			return new ScoredApplication(application, 0, false);
		}

		String roleTitle = normalize(application.getRoleTitle());
		boolean roleMatched = containsPhrase(searchableText, roleTitle);
		return new ScoredApplication(application, roleMatched ? 100 : 70, roleMatched);
	}

	private boolean containsPhrase(String text, String phrase) {
		return !phrase.isBlank() && (" " + text + " ").contains(" " + phrase + " ");
	}

	private String normalize(String value) {
		return value.toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", " ")
				.trim()
				.replaceAll("\\s+", " ");
	}

	private record ScoredApplication(
			JobApplicationEntity application,
			int score,
			boolean roleMatched) {
	}
}
