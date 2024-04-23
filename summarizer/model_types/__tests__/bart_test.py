import unittest
from unittest.mock import patch, MagicMock
from model_types.bart import summarize


class TestSummarizeInputType(unittest.TestCase):
    @patch("model_types.bart.BartForConditionalGeneration.from_pretrained")
    @patch("model_types.bart.BartTokenizer.from_pretrained")
    def test_input_to_tokenizer_is_string(self, mock_tokenizer, mock_model):
        """Should ensure the input to the model is a string."""
        mock_prepare_seq2seq_batch = MagicMock()

        mock_tokenizer_instance = MagicMock()
        mock_tokenizer_instance.prepare_seq2seq_batch = mock_prepare_seq2seq_batch
        mock_tokenizer.return_value = mock_tokenizer_instance

        # Mock model to prevent actual model loading and summarization
        mock_model.return_value = MagicMock()

        messages = ["Message one.", "Message two."]

        summarize(messages)

        mock_prepare_seq2seq_batch.assert_called_once()

        # Get the first argument passed to prepare_seq2seq_batch on its last call
        args, _ = mock_prepare_seq2seq_batch.call_args
        reviews_text = args[0]

        self.assertIsInstance(reviews_text, str, "Should ensure the input to the summarizer model should be a string")


if __name__ == '__main__':
    unittest.main()
