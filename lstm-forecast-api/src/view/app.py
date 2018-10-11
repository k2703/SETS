import helpers




def load_state(train_values_name, model_name):
    train_values = helpers.load_ndarray(train_values_name)
    lstm_model = helpers.load_model(model_name)
    return train_values, lstm_model







if __name__ == "__main__":
    train_vaues, lstm_model = load_state('FGE',"FGE_model_1_train_state")





'''
#!flask/bin/pythonp
from flask import Flask, jsonify

app = Flask(__name__)

tasks = [
    {
        'id': 1,
        'title': u'Buy groceries',
        'description': u'Milk, Cheese, Pizza, Fruit, Tylenol', 
        'done': False
    },
    {
        'id': 2,
        'title': u'Learn Python',
        'description': u'Need to find a good Python tutorial on the web', 
        'done': False
    }
]

@app.route('/todo/api/v1.0/tasks', methods=['GET'])
def get_tasks():
    return jsonify({'tasks': tasks})


@app.route('/todo/api/v1.0/tasks/<int:task_id>', methods=['GET'])
def get_task(task_id):
    task = [task for task in tasks if task['id'] == task_id]
    if len(task) == 0:
        abort(404)
    return jsonify({'task': task[0]})


 # http://127.0.0.1:5000/todo/api/v1.0/tasks

if __name__ == '__main__':
    app.run(debug=True)
'''
