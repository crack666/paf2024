import { request } from 'http';

const options = {
  hostname: 'localhost',
  port: 8080,
  path: '/api/users',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  }
};

const data = JSON.stringify({
  username: 'testuser',
  email: 'test@example.com'
});

const req = request(options, (res) => {
  console.log(`Status: ${res.statusCode}`);
  
  let responseData = '';
  
  res.on('data', (chunk) => {
    responseData += chunk;
  });
  
  res.on('end', () => {
    console.log('Response Body:', responseData);
  });
});

req.on('error', (error) => {
  console.error('Error:', error);
});

req.write(data);
req.end();