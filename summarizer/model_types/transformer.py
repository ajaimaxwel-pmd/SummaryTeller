from transformers import T5Tokenizer, T5ForConditionalGeneration


def summarize(messages):
    try:
        # Load pre-trained T5 model and tokenizer
        model_name = "t5-small"
        tokenizer = T5Tokenizer.from_pretrained(model_name)
        model = T5ForConditionalGeneration.from_pretrained(model_name)
    except Exception as e:
        print(f"Error loading model or tokenizer: {e}")
        return ""

    try:
        input_text = "\n".join(messages)
        # Tokenize and summarize the input text using T5
        inputs = tokenizer.encode("summarize: " + input_text, return_tensors="pt", max_length=1024, truncation=True)
        summary_ids = model.generate(inputs, max_length=150, min_length=50, length_penalty=2.0, num_beams=4, early_stopping=True)

        # Decode and output the summary
        summary = tokenizer.decode(summary_ids[0], skip_special_tokens=True)
        return summary
    except Exception as e:
        print(f"Error during summarization: {e}")
        return ""
