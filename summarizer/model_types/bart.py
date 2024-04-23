from transformers import BartTokenizer, BartForConditionalGeneration


def summarize(messages):
    try:
        # Load BART model and tokenizer
        model_name = "facebook/bart-large-cnn"
        tokenizer = BartTokenizer.from_pretrained(model_name)
        model = BartForConditionalGeneration.from_pretrained(model_name)
    except Exception as e:
        print(f"Error loading model or tokenizer: {e}")
        return ""

    try:
        print(f'Summarizing with the Bart model: {model_name}')
        # TODO:
        # Preprocess the reviews
        # Add cleaning and tokenization steps here

        # Combine reviews into a single string with separator tokens
        reviews_text = "\n".join(messages)

        # Encode the reviews as input for BART
        inputs = tokenizer.prepare_seq2seq_batch(reviews_text, return_tensors="pt")

        # Generate summary using BART model
        summary_ids = model.generate(**inputs)

        # Decode the generated summary tokens back to text
        summary_text = tokenizer.decode(summary_ids[0], skip_special_tokens=True)

        return summary_text
    except Exception as e:
        print(f"Error during summarization: {e}")
        return ""
