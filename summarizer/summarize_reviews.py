from model_types.bart import summarize as summarize_with_bart
from model_types.transformer import summarize as summarize_with_transformer

summarization_strategies = {
    "bart": summarize_with_bart,
    "transformer": summarize_with_transformer,
}


def summarize_reviews(reviews, strategy_key):
    if strategy_key in summarization_strategies:
        return summarization_strategies[strategy_key](reviews)
    else:
        raise ValueError("Unknown summarization strategy")
