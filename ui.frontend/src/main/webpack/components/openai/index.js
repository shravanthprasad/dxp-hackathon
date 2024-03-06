import OpenAI from 'openai';

const assistantId = 'asst_SYgA7NtuVgRDMM0vs2QUqItS';
const role = 'user';

const openai = new OpenAI({
    organization: 'org-6c5PwCe4AxBXmSBu9Te5YF5d',
    apiKey: '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',
    dangerouslyAllowBrowser: true
});

async function chat(prompt) {
    const context = document.getElementById('js-chatbot-output')?.innerHTML;
    console.log('context ', context);
    const completion = await openai.chat.completions.create({
        messages: [{
            'role': 'system',
            'content': 'Answer user query only from provided context. Your answer should be HTML formatted in the best way. Else Reply Sorry, I don\'t have the information'
        },
            {'role': 'user', 'content': context},
            {'role': 'assistant', 'content': 'Thank you for providing me context. Pls ask me any query.'},
            {'role': 'user', 'content': prompt}],
        model: 'gpt-3.5-turbo',
    });
    console.log(completion);
    console.log(completion?.choices[0]);
    const data = completion?.choices[0]?.message;
    let messageContent = document.querySelector('.js-chatbot-content');
    messageContent.innerHTML += '<b class="text-primary">User</b><p class="text-body">' + prompt + '</p>';
    messageContent.innerHTML += '<b class="text-primary">Assistant</b><p class="text-body">' + data.content + '</p>';
    console.log('output', data);
    document.getElementById('spinner').classList.add('d-none');
    document.querySelector('.js-chatbot-message').value = '';

}

function initOpenAi() {
    const messageInput = document.querySelector('.js-chatbot-message');
    const submitMessage = document.querySelector('.js-chatbot-submit');
    const clearButton = document.querySelector('.js-chatbot-clear');

    clearButton.addEventListener('click', (event) => {
        event.preventDefault();
        window.threadId = null;
    });


    submitMessage.addEventListener('click', (event) => {
        console.log('adding listener');
        event.preventDefault();
        document.getElementById('spinner').classList.remove('d-none');
        chat(messageInput.value).then();
    });

}

window.initOpenAi = initOpenAi;
window.openai = openai;

document.addEventListener('DOMContentLoaded', initOpenAi);
