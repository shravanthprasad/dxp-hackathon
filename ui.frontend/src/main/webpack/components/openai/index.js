import OpenAI from 'openai';
import showdown from 'showdown';

const assistantId = 'asst_SYgA7NtuVgRDMM0vs2QUqItS';
const role = 'user';

const openai = new OpenAI({
    organization: 'org-6c5PwCe4AxBXmSBu9Te5YF5d',
    apiKey: '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',
    dangerouslyAllowBrowser: true
});


/*async function getContent(fileId) {
    const file = await openai.beta.assistants.files.retrieve(
        'asst_SYgA7NtuVgRDMM0vs2QUqItS',
        fileId
    );
    console.log(file);
}*/

async function sendMessage(message) {
    // create Thread and Run
    let run;
    document.getElementById('spinner').classList.remove('d-none');
    if (window.threadId) {
        // If thread present, add message to thread
        const threadMessages = await openai.beta.threads.messages.create(
            window.threadId,
            {role: role, content: message}
        );
        console.log('threadMessages', threadMessages);
        // create run
        run = await openai.beta.threads.runs.create(
            window.threadId,
            {assistant_id: assistantId}
        );
    } else {
        //
        run = await openai.beta.threads.createAndRun({
            assistant_id: assistantId,
            thread: {
                messages: [
                    {role: role, content: message},
                ],
            },
        });
    }
    console.log('run', run);
    window.threadId = run.thread_id;
    window.runId = run.id;
    // check run status
    pollResponse().then((data) => {
        // write data to output
        let messageContent = document.querySelector('.js-chatbot-content');
        let output = '';
        for (let i = 0; i < data.length; i++) {
            let contentMarkdown = data[i].content[0].text.value;
            let converter = new showdown.Converter();
            const contentInHtml = converter.makeHtml(contentMarkdown);
            if (data[i].role === 'user') {
                output += '<b class="text-primary">User</b><br/><p class="text-body">' + contentInHtml + '</p>';
            } else {
                output += '<b class="text-success">Assistant</b><br/><p class="text-dark">' + contentInHtml + '</p>';
            }
        }
        messageContent.innerHTML = output;
        console.log('output', data);
        /*document.querySelector('.assistant-model-content').querySelectorAll('a').forEach(ele => {
            console.log(ele.getAttribute('href'));
            const href = ele.getAttribute('href');
            const fileId = href.substring(href.lastIndexOf('/'));
            ele.addEventListener('click', (event) => {
                event.preventDefault();
                getContent(fileId).then(() => console.log('got file'));
            });
        });*/
        document.getElementById('spinner').classList.add('d-none');
    });
}

async function pollResponse() {
    let i = 0, result = {};
    while ((i < 5) && (result.status !== 'completed')) {
        try {
            result = await openai.beta.threads.runs.retrieve(
                window.threadId,
                window.runId
            );
            // Process the result or perform any actions
            console.log('Result:', result);
        } catch (error) {
            // Handle errors
            console.error('Error:', error);
        }
        // Wait for 10 seconds before the next iteration
        await delay(10000);
    }

    // after polling return thread messages list
    const threadMessages = await openai.beta.threads.messages.list(
        window.threadId
    );

    console.log('threadMessages', threadMessages.data);
    return threadMessages.data;
}

// Helper function to create a delay using Promise
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/*async function sendMessage(message) {
    const completion = await openai.chat.completions.create({
        messages: [{role: 'system', content: message}],
        model: 'gpt-3.5-turbo',
    });
    let messageContent = document.querySelector('.js-chatbot-content');
    messageContent.innerHTML = completion?.choices[0]?.message?.content;
    console.log(completion.choices[0]);
}*/

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
        sendMessage(messageInput.value).then();
    });
}

window.initOpenAi = initOpenAi;
window.openai = openai;

document.addEventListener('DOMContentLoaded', initOpenAi);
