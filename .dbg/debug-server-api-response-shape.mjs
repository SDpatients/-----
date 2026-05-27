import http from 'node:http'
import fs from 'node:fs'
import path from 'node:path'

const sessionId = 'api-response-shape'
const outdir = path.resolve('.dbg')
fs.mkdirSync(outdir, { recursive: true })
const logFile = path.join(outdir, `trae-debug-log-${sessionId}.ndjson`)
fs.writeFileSync(logFile, '')

const writeEnv = (port) => {
  fs.writeFileSync(path.join(outdir, `${sessionId}.env`), `DEBUG_SERVER_URL=http://127.0.0.1:${port}/event\nDEBUG_SESSION_ID=${sessionId}\n`)
}

const tryListen = (port) => {
  const server = http.createServer((req, res) => {
    res.setHeader('Access-Control-Allow-Origin', '*')
    res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS, GET')
    res.setHeader('Access-Control-Allow-Headers', 'Content-Type')
    if (req.method === 'OPTIONS') {
      res.writeHead(204)
      res.end()
      return
    }
    if (req.method === 'GET' && req.url === '/health') {
      res.writeHead(200, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify({ ok: true, sessionId }))
      return
    }
    if (req.method === 'POST' && req.url === '/event') {
      let body = ''
      req.on('data', (chunk) => { body += chunk })
      req.on('end', () => {
        try {
          const event = JSON.parse(body || '{}')
          fs.appendFileSync(logFile, `${JSON.stringify({ ts: Date.now(), ...event })}\n`)
          res.writeHead(200)
          res.end('ok')
        } catch {
          res.writeHead(400)
          res.end('bad json')
        }
      })
      return
    }
    res.writeHead(404)
    res.end('not found')
  })
  server.on('error', () => tryListen(port + 1))
  server.listen(port, '127.0.0.1', () => {
    writeEnv(port)
    console.log(`@@DEBUG_SERVER_INFO\n${JSON.stringify({ api_url: `http://127.0.0.1:${port}/event`, session_id: sessionId, log_file: logFile, env_file: path.join(outdir, `${sessionId}.env`) }, null, 2)}\n@@END_DEBUG_SERVER_INFO`)
  })
}

tryListen(7777)
